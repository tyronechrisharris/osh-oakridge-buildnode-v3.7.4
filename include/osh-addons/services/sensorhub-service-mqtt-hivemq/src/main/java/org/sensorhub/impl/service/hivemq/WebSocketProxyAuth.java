/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2026 Botts Innovative Research, Inc. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.hivemq;

import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;


class WebSocketProxyAuth
{
    private static final ConcurrentMap<InetAddress, Queue<String>> pendingUsers = new ConcurrentHashMap<>();


    private WebSocketProxyAuth()
    {
    }


    static void register(InetAddress mqttClientAddress, String userID)
    {
        if (mqttClientAddress == null || userID == null)
            return;

        pendingUsers.computeIfAbsent(mqttClientAddress, k -> new ConcurrentLinkedQueue<>()).add(userID);
    }


    static String consume(InetAddress mqttClientAddress)
    {
        if (mqttClientAddress == null)
            return null;

        var users = pendingUsers.get(mqttClientAddress);
        if (users == null)
            return null;

        var userID = users.poll();
        if (users.isEmpty())
            pendingUsers.remove(mqttClientAddress, users);

        return userID;
    }
}
