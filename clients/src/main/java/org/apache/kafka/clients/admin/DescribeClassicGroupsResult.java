/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.kafka.clients.admin;

import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.annotation.InterfaceStability;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;


/**
 * The result of the {@link Admin#describeClassicGroups(Collection, DescribeClassicGroupsOptions)}} call.
 * <p>
 * The API of this class is evolving, see {@link Admin} for details.
 */
@InterfaceStability.Evolving
public class DescribeClassicGroupsResult {

    private final Map<String, KafkaFuture<ClassicGroupDescription>> futures;

    public DescribeClassicGroupsResult(final Map<String, KafkaFuture<ClassicGroupDescription>> futures) {
        this.futures = futures;
    }

    /**
     * Return a map from group id to futures which yield group descriptions.
     */
    public Map<String, KafkaFuture<ClassicGroupDescription>> describedGroups() {
        return new HashMap<>(futures);
    }

    /**
     * Return a future which yields all ClassicGroupDescription objects, if all the describes succeed.
     */
    public KafkaFuture<Map<String, ClassicGroupDescription>> all() {
        return KafkaFuture.allOf(futures.values().toArray(new KafkaFuture[0])).thenApply(
            nil -> {
                Map<String, ClassicGroupDescription> descriptions = new HashMap<>(futures.size());
                futures.forEach((key, future) -> {
                    try {
                        descriptions.put(key, future.get());
                    } catch (InterruptedException | ExecutionException e) {
                        // This should be unreachable, since the KafkaFuture#allOf already ensured
                        // that all of the futures completed successfully.
                        throw new RuntimeException(e);
                    }
                });
                return descriptions;
            });
    }
}
