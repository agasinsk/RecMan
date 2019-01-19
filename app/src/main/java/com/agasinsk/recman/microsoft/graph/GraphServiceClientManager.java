/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */
package com.agasinsk.recman.microsoft.graph;

import android.content.Context;
import android.util.Log;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.core.DefaultClientConfig;
import com.microsoft.graph.core.IClientConfig;
import com.microsoft.graph.extensions.GraphServiceClient;
import com.microsoft.graph.extensions.IGraphServiceClient;
import com.microsoft.graph.http.IHttpRequest;

/**
 * Singleton class that manages a GraphServiceClient object.
 * It implements IAuthentication provider to authenticate requests using an access token.
 */
public class GraphServiceClientManager implements IAuthenticationProvider {
    private final Context mContext;
    private IGraphServiceClient mGraphServiceClient;
    private static GraphServiceClientManager INSTANCE;

    private GraphServiceClientManager(Context context) {
        mContext = context;
    }

    /**
     * Appends an access token obtained from the {@link AuthenticationManager} class to the
     * Authorization header of the request.
     *
     */
    @Override
    public void authenticateRequest(IHttpRequest request) {
        try {
            request.addHeader("Authorization", "Bearer "
                    + AuthenticationManager.getInstance(mContext)
                    .getAccessToken());

            Log.i("Connect", "Request: " + request.toString());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static synchronized GraphServiceClientManager getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new GraphServiceClientManager(context);
        }
        return INSTANCE;
    }

    public synchronized IGraphServiceClient getGraphServiceClient() {
        return getGraphServiceClient(this);
    }

    private synchronized IGraphServiceClient getGraphServiceClient(IAuthenticationProvider authenticationProvider) {
        if (mGraphServiceClient == null) {
            IClientConfig clientConfig = DefaultClientConfig.createWithAuthenticationProvider(
                    authenticationProvider
            );
            mGraphServiceClient = new GraphServiceClient.Builder().fromConfig(clientConfig).buildClient();
        }

        return mGraphServiceClient;
    }
}
