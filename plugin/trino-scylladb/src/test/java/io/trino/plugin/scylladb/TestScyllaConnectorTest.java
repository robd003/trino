/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.plugin.scylladb;

import com.google.common.collect.ImmutableMap;
import io.trino.Session;
import io.trino.plugin.cassandra.BaseCassandraConnectorTest;
import io.trino.testing.MaterializedResult;
import io.trino.testing.QueryRunner;

import java.sql.Timestamp;

import static io.trino.plugin.cassandra.CassandraTestingUtils.createTestTables;
import static io.trino.plugin.scylladb.ScyllaQueryRunner.createScyllaQueryRunner;
import static io.trino.plugin.scylladb.ScyllaQueryRunner.createSession;
import static io.trino.plugin.scylladb.TestingScyllaServer.V3_TAG;

public class TestScyllaConnectorTest
        extends BaseCassandraConnectorTest
{
    protected static final Session SESSION = createSession(KEYSPACE);

    private TestingScyllaServer server;

    @Override
    protected QueryRunner createQueryRunner()
            throws Exception
    {
        server = closeAfterClass(new TestingScyllaServer(V3_TAG));
        session = server.getSession();
        createTestTables(session, KEYSPACE, Timestamp.from(TIMESTAMP_VALUE.toInstant()));
        return createScyllaQueryRunner(
                server,
                ImmutableMap.of(),
                ImmutableMap.of("cassandra.batch-size", "50"), // The default 100 causes 'Batch too large' error
                REQUIRED_TPCH_TABLES);
    }

    @Override
    protected void refreshSizeEstimates(String keyspace, String tableName)
            throws Exception
    {
        server.refreshSizeEstimates(KEYSPACE, tableName);
    }

    @Override
    protected MaterializedResult execute(String sql)
    {
        return getQueryRunner().execute(SESSION, sql);
    }
}
