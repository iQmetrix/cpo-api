/*
 * Copyright (C) 2003-2012 David E. Berry
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * A copy of the GNU Lesser General Public License may also be found at
 * http://www.gnu.org/licenses/lgpl.txt
 */
package org.synchronoss.cpo.jta;

import javax.transaction.xa.Xid;

/**
 * Created by dberry on 3/9/15.
 */
public class CpoXaState<T> {

  public static final int XA_UNASSIGNED = 0;
  public static final int XA_ASSIGNED = 1;
  public static final int XA_SUSPENDED = 2;


  private Xid xid;
  private T resource;
  private int state;
  private boolean success;
  private CpoBaseXaResource<T> assignedResourceManager;

  private CpoXaState(){

  }

  public CpoXaState(Xid xid, T resource, int state, CpoBaseXaResource<T> assignedResourceManager, boolean success) {
    this.xid = xid;
    this.resource = resource;
    this.state = state;
    this.assignedResourceManager = assignedResourceManager;
    this.success = success;
  }

  public void update(int state, CpoBaseXaResource<T> assignedResourceManager, boolean success) {
    this.state = state;
    this.assignedResourceManager = assignedResourceManager;
    this.success = success;
  }

  public Xid getXid() {
    return xid;
  }

  public T getResource() {
    return resource;
  }

  public int getState() {
    return state;
  }

  public boolean isSuccess() {
    return success;
  }

  public CpoBaseXaResource<T> getAssignedResourceManager() {
    return assignedResourceManager;
  }
}
