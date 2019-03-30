/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hbase.procedure2;

import org.apache.hadoop.hbase.procedure2.util.StringUtils;
import org.apache.hadoop.hbase.util.EnvironmentEdgeManager;
import org.apache.yetus.audience.InterfaceAudience;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@InterfaceAudience.Private
abstract class StoppableThread extends Thread {

  private static final Logger LOG = LoggerFactory.getLogger(StoppableThread.class);

  public StoppableThread(final ThreadGroup group, final String name) {
    super(group, name);
  }

  public abstract void sendStopSignal();

  public void awaitTermination() {
    try {
      final long startTime = EnvironmentEdgeManager.currentTime();
      for (int i = 0; isAlive(); ++i) {
        sendStopSignal();
        join(250);
        // Log every two seconds; send interrupt too.
        if (i > 0 && (i % 8) == 0) {
          LOG.warn("Waiting termination of thread {}, {}; sending interrupt", getName(),
            StringUtils.humanTimeDiff(EnvironmentEdgeManager.currentTime() - startTime));
          interrupt();
        }
      }
    } catch (InterruptedException e) {
      LOG.warn("{} join wait got interrupted", getName(), e);
    }
  }
}
