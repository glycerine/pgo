package distsys

// This implements the a barrier protocol across all PlusCal processes.
// When a PlusCal specification is compiled to TLA+, the TLC model checker
// starts every "process" defined in PlusCal at the same time.
//
// The synchronization barrier implemented here makes sure that a processes
// only starts running once all other processes declared to be ready to
// continue executing as well.
//
// For more information, see: https://github.com/UBC-NSS/pgo/wiki/Synchronization-Barrier-Protocol

import (
	"sync/atomic"
	"time"
)

const (
	RPC_ID = "PlusCalProcess"
)

// SyncBarrierRPC provides RPC methods that are used to synchronize all PlusCal processes
// in the system.
type SyncBarrierRPC struct {
	Peers          []string     // the list of peers (PlusCal processes)
	Self           string       // the identification of the current process (IP:port)
	Coordinator    string       // which host is the coordinator (IP:port)
	processesReady int32        // how many processes are ready currently (used only by the coordinator)
	readyChan      chan bool    // underlying channel used to coordinate start
	connections    *Connections // connections to other peers
	setup          bool         // whether the RPC configuration has been performed
}

// SyncBarrier wraps the underlying RPC object (SyncBarrierRPC) and provides functions that
// callers can use when a new barrier is required.
type SyncBarrier struct {
	*SyncBarrierRPC
}

// NewSyncBarrier creates a new SyncBarrier struct that can be used to create synchronization
// barriers across all processes in the PlusCal spec.
func NewSyncBarrier(peers []string, self, coordinator string) *SyncBarrier {
	barrier := &SyncBarrierRPC{
		Peers:          peers,
		Self:           self,
		Coordinator:    coordinator,
		processesReady: 0,
		readyChan:      make(chan bool, 1),
		connections:    NewConnections(self),
		setup:          false,
	}

	return &SyncBarrier{barrier}
}

func (barrier *SyncBarrier) isCoordinator() bool {
	return barrier.Self == barrier.Coordinator
}

// WaitPeers is the method to be used when the caller wants the process to block
// until all processes reach the same barrier.
func (barrier *SyncBarrier) WaitPeers() error {
	if !barrier.setup {
		barrier.init()
	}

	// the process itself is ready. Incremented when other processes indicate they are
	// ready via the `ProcessReady` RPC call
	atomic.AddInt32(&barrier.processesReady, 1)

	if barrier.isCoordinator() {
		// if this is the coordinator process and every other process already "checked-in",
		// we are ready to move on
		if err := barrier.check(); err != nil {
			return err
		}

	} else {
		barrier.helloCoordinator()
	}

	// wait for all processes to be ready
	<-barrier.readyChan

	// reset counter of processes ready so that this method can be called multiple
	// times whenever a synchronization barrier is required across all processes
	// in the system
	atomic.StoreInt32(&barrier.processesReady, 0)

	return nil
}

// exposes the RPC methods required for the processes to communicate
func (barrier *SyncBarrier) init() error {
	barrier.setup = true
	return barrier.connections.ExposeImplementation(RPC_ID, barrier.SyncBarrierRPC)
}

// check checks whether all processes have "checked-in". Only called
// by the coordinator process. If the number of "ProcessReady" calls
// the coordinator has received indicate that all nodes in the system
// are ready, the coordinator will send a message to all nodes
// indicating that they can resume their operation, and the
// coordinator itself will return from its call to WaitPeers().
func (barrier *SyncBarrierRPC) check() error {
	if int(atomic.LoadInt32(&barrier.processesReady)) == len(barrier.Peers) {
		for _, id := range barrier.Peers {
			if id == barrier.Self {
				continue
			}

			if err := barrier.connections.ConnectTo(id); err != nil {
				return err
			}

			client := barrier.connections.GetConnection(id)
			var ok bool
			if err := client.Call(RPC_ID+".Start", 0, &ok); err != nil {
				return err
			}
		}

		barrier.readyChan <- true
	}

	return nil
}

func (barrier *SyncBarrier) helloCoordinator() {
	// try to reach the coordinator until it finally succeeds.
	// Connection errors are interpreted to mean the coordinator is not up yet,
	// so we wait (indefinitely) for it to be.
	for {
		if err := barrier.connections.ConnectTo(barrier.Coordinator); err != nil {
			time.Sleep(1 * time.Second)
			continue
		}

		client := barrier.connections.GetConnection(barrier.Coordinator)
		var ok bool
		if err := client.Call(RPC_ID+".ProcessReady", barrier.Self, &ok); err != nil {
			continue
		}

		return
	}
}

// ProcessReady is an RPC method used by processes to indicate they
// are ready to run. The coordinator keeps a counter of how many
// processes are ready, and when the counter is equal to the (static)
// number of peers, it means all processes are up and ready to
// run. Send a message to each of them indicating that they may start
// running their algorithms
func (barrier *SyncBarrierRPC) ProcessReady(id string, ok *bool) error {
	atomic.AddInt32(&barrier.processesReady, 1)
	*ok = true

	return barrier.check()
}

// Start is an RPC method that is sent to every process when the
// coordinator detects that every process in the system is ready
func (barrier *SyncBarrierRPC) Start(_ int, ok *bool) error {
	barrier.readyChan <- true
	*ok = true

	return nil
}
