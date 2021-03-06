/*
 * Copyright 2014 NAVER Corp.
 *
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

package com.navercorp.pinpoint.profiler.modifier.db.cubrid;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.DatabaseInfoTraceValue;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.modifier.db.interceptor.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class CubridConnectionModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public CubridConnectionModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public Matcher getMatcher() {
        return Matchers.newClassNameMatcher("cubrid/jdbc/driver/CUBRIDConnection");
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
        }
        try {
            InstrumentClass cubridConnection = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);

            cubridConnection.addTraceValue(DatabaseInfoTraceValue.class);

            Interceptor connectionCloseInterceptor = new ConnectionCloseInterceptor();
            cubridConnection.addGroupInterceptor("close", null, connectionCloseInterceptor, CubridScope.SCOPE_NAME);


            Interceptor statementCreateInterceptor1 = new StatementCreateInterceptor();
            cubridConnection.addGroupInterceptor("createStatement", null, statementCreateInterceptor1, CubridScope.SCOPE_NAME);

            Interceptor statementCreateInterceptor2 = new StatementCreateInterceptor();
            cubridConnection.addGroupInterceptor("createStatement", new String[]{"int", "int"}, statementCreateInterceptor2, CubridScope.SCOPE_NAME);

            Interceptor statementCreateInterceptor3 = new StatementCreateInterceptor();
            cubridConnection.addGroupInterceptor("createStatement", new String[]{"int", "int", "int"}, statementCreateInterceptor3, CubridScope.SCOPE_NAME);


            Interceptor preparedStatementCreateInterceptor1 = new PreparedStatementCreateInterceptor();
            cubridConnection.addGroupInterceptor("prepareStatement", new String[]{"java.lang.String"}, preparedStatementCreateInterceptor1, CubridScope.SCOPE_NAME);

            Interceptor preparedStatementCreateInterceptor2 = new PreparedStatementCreateInterceptor();
            cubridConnection.addGroupInterceptor("prepareStatement", new String[]{"java.lang.String", "int"}, preparedStatementCreateInterceptor2, CubridScope.SCOPE_NAME);

            Interceptor preparedStatementCreateInterceptor3 = new PreparedStatementCreateInterceptor();
            cubridConnection.addGroupInterceptor("prepareStatement", new String[]{"java.lang.String", "int[]"}, preparedStatementCreateInterceptor3, CubridScope.SCOPE_NAME);

            Interceptor preparedStatementCreateInterceptor4 = new PreparedStatementCreateInterceptor();
            cubridConnection.addGroupInterceptor("prepareStatement", new String[]{"java.lang.String", "java.lang.String[]"}, preparedStatementCreateInterceptor4, CubridScope.SCOPE_NAME);

            Interceptor preparedStatementCreateInterceptor5 = new PreparedStatementCreateInterceptor();
            cubridConnection.addGroupInterceptor("prepareStatement", new String[]{"java.lang.String", "int", "int"}, preparedStatementCreateInterceptor5, CubridScope.SCOPE_NAME);

            Interceptor preparedStatementCreateInterceptor6 = new PreparedStatementCreateInterceptor();
            cubridConnection.addGroupInterceptor("prepareStatement", new String[]{"java.lang.String", "int", "int", "int"}, preparedStatementCreateInterceptor6, CubridScope.SCOPE_NAME);

            final ProfilerConfig profilerConfig = this.getProfilerConfig();
            if (profilerConfig.isJdbcProfileCubridSetAutoCommit()) {
                Interceptor setAutoCommit = new TransactionSetAutoCommitInterceptor();
                cubridConnection.addGroupInterceptor("setAutoCommit", new String[]{"boolean"}, setAutoCommit, CubridScope.SCOPE_NAME);
            }
            if (profilerConfig.isJdbcProfileCubridCommit()) {
                Interceptor commit = new TransactionCommitInterceptor();
                cubridConnection.addGroupInterceptor("commit", null, commit, CubridScope.SCOPE_NAME);
            }
            if (profilerConfig.isJdbcProfileCubridRollback()) {
                Interceptor rollback = new TransactionRollbackInterceptor();
                cubridConnection.addGroupInterceptor("rollback", null, rollback, CubridScope.SCOPE_NAME);
            }

            if (this.logger.isInfoEnabled()) {
                this.logger.info("{} class is converted.", javassistClassName);
            }

            return cubridConnection.toBytecode();
        } catch (InstrumentException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("{} modify fail. Cause:{}", this.getClass().getSimpleName(), e.getMessage(), e);
            }
            return null;
        }
    }
}
