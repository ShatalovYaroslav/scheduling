package org.objectweb.proactive.core.body.request;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Vector;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * <p>
 * This class is a utility class allowing to deal with non functional requests.
 * It keeps a reference of the non functional requests which are in the request queue, 
 * make easy and faster the processing of those pririty requests.
 * 
 * </p><p>
 * Non functional requests are requests which do not modify the application computation.
 * For instance, the migration of an active object is considered as a non functional request.
 * There are three priotity levels :
 * NFREQUEST_IMMEDIATE_PRIORITY  : high priority, next request to serve.
 * NFREQUEST_PRIORITY : more priority than functional requests
 * NFREQUEST_NO_PRIORITY : no priority among all requests 
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2006/04/12
 * @since   ProActive 3.0.1
 *
 */
public class NonFunctionalRequestsProcessor implements RequestProcessor, Serializable {

	
	private static final long serialVersionUID = 1L;
	private AbstractList nfRequestsQueue;
	private RequestFilter immediateNFResquestFilter;
    private RequestFilter priorityNFRequestFilter;
    private int immediateNFReqNumber, priorityNFReqNumber;
    
    
	public NonFunctionalRequestsProcessor() {
		nfRequestsQueue = new Vector();
		this.immediateNFResquestFilter = new ImmediateNFRequestFilter();
        this.priorityNFRequestFilter = new PriorityNFRequestFilter();
        immediateNFReqNumber = 0;
        priorityNFReqNumber = 0;
	}
	


	/**
	 * Add a non functional request in the queue according to its priority.
	 * Immediate priority requests are on the front of the queue. 
	 * @param request to add in the queue.
	 */
	public synchronized void  addToNFRequestsQueue(Request request) {
		if(immediateNFResquestFilter.acceptRequest(request)) {
			nfRequestsQueue.add(immediateNFReqNumber,request);
			immediateNFReqNumber ++;
		} else if (priorityNFRequestFilter.acceptRequest(request)){
			nfRequestsQueue.add(immediateNFReqNumber + priorityNFReqNumber, request);
			priorityNFReqNumber ++;
		}
	}
	
	/**
	 * Returns the oldest and most priority non functional request 
	 * @param remove=true means that the request has to be removed from the lists
	 * @return the non functional request with the highest priority : the first in the list.
	 */
	public synchronized Request getOldestPriorityNFRequest(boolean remove) {
		Request request;
		if(nfRequestsQueue.isEmpty()) {
			return null;
		}else if (remove){			
			request = (Request) nfRequestsQueue.remove(0);
			if(immediateNFReqNumber > 0){
				immediateNFReqNumber --;
			}else {
				priorityNFReqNumber --;
			}
			return request;
		}else {
			return (Request) nfRequestsQueue.get(0);
		}		
	}
	
	
	/**
	 * Returns the yougest and most priority non functional request 
	 * @param remove=true means that the request has to be removed from the lists
	 * @return the youngest priority request
	 */
	public synchronized Request getYoungestPriorityNFRequest(boolean remove) {
		Request request;
		
		if(nfRequestsQueue.isEmpty()) {
			return null;
		}else if (remove){
			if(immediateNFReqNumber != 0){
				request = (Request) nfRequestsQueue.remove(immediateNFReqNumber);
				immediateNFReqNumber --;
				return request;
			}else {
				request = (Request) nfRequestsQueue.remove(priorityNFReqNumber);
				priorityNFReqNumber --;
				return request;
			}
		}else {
			if(immediateNFReqNumber != 0){
				return (Request) nfRequestsQueue.get(immediateNFReqNumber);
			}else {
				return (Request) nfRequestsQueue.get(priorityNFReqNumber);
			}
		}			
	}
	
	
	public synchronized boolean isEmpty() {
		return (immediateNFReqNumber == 0 && priorityNFReqNumber == 0);
	}
	

	public synchronized String toString() {
		StringBuffer sb = new StringBuffer("NFRequests Queue : ");
		sb.append("--- NonFunctionalRequestQueue n=").append(nfRequestsQueue.size()).append("   requests --- ->\n");
		int count = 0;
		java.util.Iterator iterator = nfRequestsQueue.iterator();
		while (iterator.hasNext()) {
			Request currentrequest = (Request) iterator.next();
			sb.append(count).append("--> ")
			.append(currentrequest.getMethodName()).append("\n");
			count++;
		}
		sb.append("--- End NonFunctionalRequestQueue ---");
		return sb.toString();
	}
	
	public int processRequest(Request request) {
		if((request.getNFRequestPriority() == Request.NFREQUEST_IMMEDIATE_PRIORITY )|| (request.getNFRequestPriority() == Request.NFREQUEST_PRIORITY)){
			nfRequestsQueue.add(request);
		}
		return RequestProcessor.KEEP;
	}
	  
	// --------------- FILTERS -----------------//
	
	
	/**
     * ImmediateNFRequestFilter is a RequestFilter that matches immediate non functional requests
     *
     * @author  ProActive Team
     * @version 1.0,  2006/03/03
     * @since   ProActive 3.0.1
     * @see RequestFilter
     *
     */
    protected class ImmediateNFRequestFilter implements RequestFilter, Serializable {    	
    	/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public boolean acceptRequest(Request request) {
    		return (request.isFunctionalRequest() 
    				&& (request.getNFRequestPriority() == Request.NFREQUEST_IMMEDIATE_PRIORITY));
    	} 
    }
    
    
    /**
     * PriorityNFRequestFilter is a RequestFilter that matches priority non functional requests
     *
     * @author  ProActive Team
     * @version 1.0,  2006/03/03
     * @since   ProActive 3.0.1
     * @see RequestFilter
     *
     */
    protected class PriorityNFRequestFilter implements RequestFilter, Serializable {    	
    	/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public boolean acceptRequest(Request request) {
    		return (request.isFunctionalRequest() 
    				&& (request.getNFRequestPriority() == Request.NFREQUEST_PRIORITY));
    	} 
    }
    
    /**
     * NoPriorityNFRequestFilter is a RequestFilter that matches no priority non functional requests
     *
     * @author  ProActive Team
     * @version 1.0,  2006/03/03
     * @since   ProActive 3.0.1
     * @see RequestFilter
     *
     */
    protected class NoPriorityNFRequestFilter implements RequestFilter, Serializable {    	
    	/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public boolean acceptRequest(Request request) {
    		return (request.isFunctionalRequest() 
    				&& (request.getNFRequestPriority() == Request.NFREQUEST_NO_PRIORITY));
    	} 
    	
    }	
 
//  --------------- END OF FILTERS -----------------//
    
    
  



    
    
}
