package org.everit.osgi.jdbc.postgresql;

/*
 * Copyright (c) 2011, Everit Kft.
 *
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.osgi.service.jdbc.DataSourceFactory;
import org.postgresql.Driver;
import org.postgresql.ds.PGPoolingDataSource;
import org.postgresql.xa.PGXADataSource;

public class PgDataSourceFactory implements DataSourceFactory {

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(final Properties props) throws SQLException {
        throw new UnsupportedOperationException("PG XADataSource not supported currently");
    }

    @Override
    public DataSource createDataSource(final Properties props) throws SQLException {
        return createJdbcDataSource(props);
    }

    @Override
    public java.sql.Driver createDriver(final Properties props) throws SQLException {
        if ((props != null) && (props.size() > 0)) {
            throw new SQLException("Posgres JDBC driver does not support any property to be set");
        }
        Driver driver = new Driver();
        return driver;
    }

    protected PGPoolingDataSource createJdbcDataSource(final Properties props) throws SQLException {
        String serverName = null;
        Integer portNumber = null;
        String databaseName = null;
        String user = null;
        String password = null;

        // FIXME Property DataSourceFactory.JDBC_URL should be resolved much more better, currently the following format
        // is supported: "serverName:portNumber:databaseName"
        for (String key : props.stringPropertyNames()) {
            if (DataSourceFactory.JDBC_URL.equals(key)) {
                String[] jdbcUrl = props.getProperty(key).split(":");
                serverName = jdbcUrl[0];
                portNumber = Integer.valueOf(jdbcUrl[1]);
                databaseName = jdbcUrl[2];
            } else if (DataSourceFactory.JDBC_USER.equals(key)) {
                user = props.getProperty(key);
            } else if (DataSourceFactory.JDBC_PASSWORD.equals(key)) {
                password = props.getProperty(key);
            } else {
                throw new SQLException("Unsupported JDBC property for Postgres: " + key);
            }
        }
        PGPoolingDataSource dataSource = new PGPoolingDataSource();
        if (databaseName != null) {
            dataSource.setDatabaseName(databaseName);
        }
        if (portNumber != null) {
            dataSource.setPortNumber(portNumber);
        }
        if (serverName != null) {
            dataSource.setServerName(serverName);
        }
        if (user != null) {
            dataSource.setUser(user);
        }
        if (password != null) {
            dataSource.setPassword(password);
        }
        return dataSource;
    }

    @Override
    public XADataSource createXADataSource(final Properties props) throws SQLException {
        // FIXME this should be solved better
        PGPoolingDataSource jdbcDataSource = createJdbcDataSource(props);
        PGXADataSource pgxaDataSource = new PGXADataSource();
        pgxaDataSource.setDatabaseName(jdbcDataSource.getDatabaseName());
        pgxaDataSource.setPortNumber(jdbcDataSource.getPortNumber());
        pgxaDataSource.setServerName(jdbcDataSource.getServerName());
        pgxaDataSource.setUser(jdbcDataSource.getUser());
        pgxaDataSource.setPassword(jdbcDataSource.getPassword());
        return pgxaDataSource;
    }

}
