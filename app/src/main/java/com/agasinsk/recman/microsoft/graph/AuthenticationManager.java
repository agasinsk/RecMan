/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */
package com.agasinsk.recman.microsoft.graph;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.AuthenticationResult;
import com.microsoft.identity.client.MsalException;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.User;

/**
 * Handles setup of OAuth library in API clients.
 */
public class AuthenticationManager {
    private static final String TAG = "AuthenticationManager";
    private static AuthenticationManager INSTANCE;
    private static PublicClientApplication mPublicClientApplication;
    private AuthenticationResult mAuthResult;
    private MicrosoftAuthenticationCallback mActivityCallback;

    private AuthenticationManager() {
    }

    public static synchronized AuthenticationManager getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new AuthenticationManager();
            if (mPublicClientApplication == null) {
                mPublicClientApplication = new PublicClientApplication(context);
            }
        }
        return INSTANCE;
    }

    private static synchronized void resetInstance() {
        INSTANCE = null;
    }

    /**
     * Returns the access token obtained in authentication
     *
     * @return mAccessToken
     */
    public String getAccessToken() {
        return mAuthResult.getAccessToken();
    }

    public PublicClientApplication getPublicClient() {
        return mPublicClientApplication;
    }

    /**
     * Disconnects the app from Office 365 by clearing the token cache, setting the client objects
     * to null, and removing the user id from shred preferences.
     */
    public void disconnect() {
        mPublicClientApplication.remove(mAuthResult.getUser());
        // Reset the AuthenticationManager object
        AuthenticationManager.resetInstance();
    }

    /**
     * Authenticates the user and lets the user authorize the app for the requested permissions.
     * An authentication token is returned via the getAuthInteractiveCalback method
     *
     */
    public void callAcquireToken(Activity activity, final MicrosoftAuthenticationCallback authenticationCallback) {
        mActivityCallback = authenticationCallback;
        mPublicClientApplication.acquireToken(
                activity, Constants.SCOPES, getAuthInteractiveCallback());
    }

    public void callAcquireTokenSilent(User user, boolean forceRefresh, MicrosoftAuthenticationCallback msalAuthenticationCallback) {
        mActivityCallback = msalAuthenticationCallback;
        mPublicClientApplication.acquireTokenSilentAsync(Constants.SCOPES, user, null, forceRefresh, getAuthSilentCallback());
    }


    /* Callback method for acquireTokenSilent calls
     * Looks if tokens are in the cache (refreshes if necessary and if we don't forceRefresh)
     * else errors that we need to do an interactive request.
     */
    private AuthenticationCallback getAuthSilentCallback() {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                /* Successfully got a token, call Graph now */
                Log.d(TAG, "Successfully authenticated");

                /* Store the authResult */
                mAuthResult = authenticationResult;

                //invoke UI callback
                if (mActivityCallback != null)
                    mActivityCallback.onMicrosoftAuthenticationSuccess(mAuthResult);
            }

            @Override
            public void onError(MsalException exception) {
                /* Failed to acquireToken */
                Log.d(TAG, "Authentication failed: " + exception.toString());
                if (mActivityCallback != null)
                    mActivityCallback.onMicrosoftAuthenticationError(exception);
            }

            @Override
            public void onCancel() {
                /* User canceled the authentication */
                Log.d(TAG, "User cancelled login.");
            }
        };
    }

    /* Callback used for interactive request.  If succeeds we use the access
     * token to call the Microsoft Graph. Does not check cache
     */
    private AuthenticationCallback getAuthInteractiveCallback() {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                /* Successfully got a token, call graph now */
                Log.d(TAG, "Successfully authenticated");
                Log.d(TAG, "ID Token: " + authenticationResult.getIdToken());

                /* Store the auth result */
                mAuthResult = authenticationResult;
                if (mActivityCallback != null)
                    mActivityCallback.onMicrosoftAuthenticationSuccess(mAuthResult);
            }

            @Override
            public void onError(MsalException exception) {
                /* Failed to acquireToken */
                Log.d(TAG, "Authentication failed: " + exception.toString());
                if (mActivityCallback != null)
                    mActivityCallback.onMicrosoftAuthenticationError(exception);
            }

            @Override
            public void onCancel() {
                /* User canceled the authentication */
                Log.d(TAG, "User cancelled login.");
                if (mActivityCallback != null)
                    mActivityCallback.onMicrosoftAuthenticationCancel();
            }
        };
    }
}
