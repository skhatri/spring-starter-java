package com.github.starter.config.duck;

import com.github.starter.exception.Exceptions;
import reactor.core.publisher.Flux;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DataClient {

    private final Connection conn;

    public DataClient(DataSource ds) {
        try {
            this.conn = ds.getConnection();
        } catch (Exception ex) {
            throw Exceptions.dbException(ex);
        }
    }

    public StatementExec sql(String query) {
        try {
            return new StatementExec(conn.prepareStatement(query));
        } catch (Exception ex) {
            throw Exceptions.dbException(ex);
        }
    }


    public static class StatementExec {
        private final PreparedStatement stm;

        public StatementExec(PreparedStatement stm) {
            this.stm = stm;
        }

        public StatementExec bind(int index, Object value) {
            try {
                this.stm.setObject(index, value);
            } catch (Exception e) {
                throw Exceptions.dbException(e);
            }
            return this;
        }

        public boolean execute() {
            try {
                return this.stm.execute();
            } catch (Exception e) {
                throw Exceptions.dbException(e);
            }
        }

        public <T> Flux<T> map(Function<Map<String, Object>, T> f) {
            try {
                ResultSet rs = this.stm.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                List<String> cols = new ArrayList<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    cols.add(meta.getColumnName(i));
                }
                List<T> items = new ArrayList<>();
                while (rs.next()) {
                    LinkedHashMap<String, Object> row = new LinkedHashMap<>();
                    for (String col : cols) {
                        row.put(col, rs.getObject(col));
                    }
                    items.add(f.apply(row));
                }
                return Flux.fromIterable(items);
            } catch (Exception e) {
                throw Exceptions.dbException(e);
            }
        }

    }
}
