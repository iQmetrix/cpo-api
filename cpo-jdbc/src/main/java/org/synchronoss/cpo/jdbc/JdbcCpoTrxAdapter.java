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
package org.synchronoss.cpo.jdbc;

import org.slf4j.*;
import org.synchronoss.cpo.*;
import org.synchronoss.cpo.jdbc.meta.JdbcCpoMetaDescriptor;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;

public class JdbcCpoTrxAdapter extends JdbcCpoAdapter implements CpoTrxAdapter {

  /**
   * Version Id for this class.
   */
  private static final long serialVersionUID = 1L;

  private static final Logger logger = LoggerFactory.getLogger(JdbcCpoAdapter.class);

  /**
   * DOCUMENT ME!
   */
  // Default Connection. Only used JdbcCpoTrxAdapter
  private Connection writeConnection_ = null;

  // map to keep track of busy connections
  private static final HashMap<Connection, Connection> busyMap_ = new HashMap<>();

  @SuppressWarnings("unused")
  private JdbcCpoTrxAdapter() {
  }

  protected JdbcCpoTrxAdapter(JdbcCpoMetaDescriptor metaDescriptor, boolean batchSupported, String dataSourceName, DataSource readDataSource, DataSource writeDataSource) throws CpoException {
    super(metaDescriptor, batchSupported, dataSourceName, readDataSource, writeDataSource);
    setStaticConnection(super.getWriteConnection());
  }

  @Override
  public void commit() throws CpoException {
    if (writeConnection_ != null) {
      try {
        writeConnection_.commit();
      } catch (SQLException se) {
        throw new CpoException(se.getMessage());
      }
    } else {
      throw new CpoException("Transaction Object has been Closed");
    }
  }

  @Override
  public void rollback() throws CpoException {
    if (writeConnection_ != null) {
      try {
        writeConnection_.rollback();
      } catch (Exception e) {
        throw new CpoException(e.getMessage());
      }
    } else {
      throw new CpoException("Transaction Object has been Closed");
    }
  }

  @Override
  public boolean isClosed() throws CpoException {
    boolean closed = false;

    try {
      closed = (writeConnection_ == null || writeConnection_.isClosed());
    } catch (Exception e) {
      throw new CpoException(e.getMessage());
    }
    return closed;
  }

  @Override
  public void close() throws CpoException {
    if (writeConnection_ != null) {
      try {
        try {
          writeConnection_.rollback();
        } catch (Exception e) {
          if (logger.isTraceEnabled()) {
            logger.trace(e.getLocalizedMessage());
          }
        }
        try {
          writeConnection_.close();
        } catch (Exception e) {
          if (logger.isTraceEnabled()) {
            logger.trace(e.getLocalizedMessage());
          }
        }
      } finally {
        setStaticConnection(null);
      }
    }
  }

  /**
   * DOCUMENT ME!
   */
  @Override
  protected void finalize() {
    try {
      super.finalize();
    } catch (Throwable e) {
      if (logger.isTraceEnabled()) {
        logger.trace(e.getLocalizedMessage());
      }
    }
    try {
      if (writeConnection_ != null && !writeConnection_.isClosed()) {
        this.close();
      }
    } catch (Exception e) {
      if (logger.isTraceEnabled()) {
        logger.trace(e.getLocalizedMessage());
      }
    }
  }

  protected boolean isConnectionBusy(Connection c) {
    synchronized (busyMap_) {
      Connection test = busyMap_.get(c);
      return test != null;
    }
  }

  protected void setConnectionBusy(Connection c) {
    synchronized (busyMap_) {
      busyMap_.put(c, c);
    }
  }

  protected void clearConnectionBusy(Connection c) {
    synchronized (busyMap_) {
      busyMap_.remove(c);
    }
  }

  protected Connection getStaticConnection() throws CpoException {
    if (writeConnection_ != null) {
      if (isConnectionBusy(writeConnection_)) {
        throw new CpoException("Error Connection Busy");
      }
    }
    return writeConnection_;
  }

  private void setStaticConnection(Connection c) {
    writeConnection_ = c;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   * @throws CpoException DOCUMENT ME!
   */
  @Override
  protected Connection getReadConnection() throws CpoException {
    Connection connection = getStaticConnection();

    setConnectionBusy(connection);

    return connection;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   * @throws CpoException DOCUMENT ME!
   */
  @Override
  protected Connection getWriteConnection() throws CpoException {
    Connection connection = getStaticConnection();

    setConnectionBusy(connection);

    return connection;
  }


  /**
   *
   * Closes a local connection. A CpoTrxAdapter allows the user to manage the transaction so <code>closeLocalConnection</code>
   * does nothing
   *
   * @param connection The connection to be closed
   */
  @Override
  protected void closeLocalConnection(Connection connection) {
    clearConnectionBusy(connection);
  }

  /**
   *
   * Commits a local connection. A CpoTrxAdapter allows the user to manage the transaction so <code>commitLocalConnection</code>
   * does nothing
   *
   * @param connection The connection to be committed
   */
  @Override
  protected void commitLocalConnection(Connection connection) {
  }

  /**
   *
   * Rollbacks a local connection. A CpoTrxAdapter allows the user to manage the transaction so <code>rollbackLocalConnection</code>
   * does nothing
   *
   * @param connection The connection to be rolled back
   */
  @Override
  protected void rollbackLocalConnection(Connection connection) {
  }


}
