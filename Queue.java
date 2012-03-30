

/**
 * Queue, a simple, yet powerful FIFO data structure implemented with a
 * linked-list of references.
 *
 * Copyright (C) 1999, Matt Luker
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 */



/**
 * <P>For some reason, Queue is not in collections.  Perhaps there is some
 * clever way to use <code>Stack</code>?  Maybe I'm an idiot, but it
 * bugs me, and I need a queue.  So I'm writing one.  Standard FIFO stuff.</P>
 *
 * <P>I've got two possible design paths: use an array internally or a linked
 * list.  I've chosen a linked list for now--remember, references are pointers
 * too <code>;-)</code>.  I feel this is more flexible and efficient.  I want
 * to use this queue under very intense loads; I'm concernced about doing
 * array copies under intense load.</P>
 *
 * @author Matt Luker
 * @version $Revision: 1.2 $
 */

public class Queue {

	/**
	 * <P>Individual node used by <code>Queue</code>.  The <code>Queue</code>
	 * is composed of a linked list of <code>nodes</code>.</P>
	 */
	private class Node {
		/**
		 * Constructs a <code>Node</code> with <code>value</code>.
		 *
		 * @param value the value of this <code>Node</code>.
		 */
		public Node(Object value) {
			_value=value;
			_next=null;
		}

		/**
		 * Returns the value held by this <code>Node</code>.
		 *
		 * @returns the value of the <code>Node</code>
		 */
		public Object getValue() {
			return _value;
		}

		/**
		 * Sets the value for the <code>Node</code>.
		 *
		 * @param value the value for the <code>Node</code>
		 */
		public void setValue(Object value) {
			_value=value;
		}

		/**
		 * Returns the next <code>Node</code> in the linked list.
		 *
		 * @returns the next value in the linked list
		 */
		public Node next() {
			return _next;
		}

		/**
		 * Set the next link after this <code>Node</code> in the linked list.
		 *
		 * @param next the <code>Node</code> that will be next in the list
		 */
		public synchronized void setNext(Node next) {
			_next=next;
		}

		/**
		 * The value of the <code>Node</code>.
		 */
		private Object _value;

		/**
		 * The next <code>Node</code> in the linked list.
		 */
		private Node _next;
	}

	/**
	 * Constructs a <code>Queue</code>, initializing head and tail to null
	 * (i.e. empty).
	 */
	public Queue() {
		_head=null;
		_tail=null;
	}

	/**
	 * Return the value in the top of the queue without altering it.
	 *
	 * @returns the value at the top of the queue
	 */
	public Object peek() {
		if (_head!=null) {  // if something's in the queue
			return _head.getValue();
		} else {  // if the queue is empty
			return null;
		}
	}

	/**
	 * Pop the top of the queue and returns the value.
	 *
	 * @returns the value of the top of the queue, null if the queue is empty
	 */
	public synchronized Object pop() {
		Object retval=null;
		if (_head!=null) {  // don't pop a null pointer!
			Queue.Node temp=_head;
			retval=temp.getValue();
			// Should there be a GC step here?  It's freaky just to cast it
			// adrift in the heap.
			_head=_head.next();
			if (_head==null) {  // head went off to nothing--i.e. only one element
				_tail=null;  // make tail null now too--since we are popping the last element
			}

			temp=null;  // poof!
		}
		return retval;
	}

	/**
	 * Add a value to the end of the queue.
	 *
	 * @param value the value to add to the end of the queue.
	 */
	public synchronized void push(Object value) {
		// create a new node to add to the queue linked list
		Queue.Node node=new Queue.Node(value);
		if (_tail!=null) {  // if the queue is not empty
			_tail.setNext(node);  // append to the tail node
			_tail=node;
		} else {  // if the queue is empty
			// add it to the linked list, pointing the head and tail to it
			_head=node;
			_tail=node;
		}
	}

	/**
	 * Get the size of the queue.
	 *
	 * @returns the size of the queue
	 */
	public int size() {
		// initialize to zero
		int count=0;
		Queue.Node itr=_head; // start our iterator at the head of the queue
		while (itr!=null) {  // while we have not reached the end of the linked list
			count++;
			itr=itr.next();  // move down the list
		}
		return count;
	}

	/**
	 * Return wether the queue is empty or not.
	 *
	 * @returns whether the queue is empty or not
	 */
	public boolean empty() {
		return (_head==null);
	}

	/**
	 * The top, head of the queue.
	 */
	Queue.Node _head;

	/**
	 * The bottom, tail of the queue.
	 */
	Queue.Node _tail;
}
