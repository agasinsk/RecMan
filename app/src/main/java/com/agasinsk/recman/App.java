package com.agasinsk.recman;

import android.app.Application;
import android.util.Log;

import com.microsoft.graph.authentication.IAuthenticationAdapter;
import com.microsoft.graph.authentication.MSAAuthAndroidAdapter;
import com.microsoft.graph.core.DefaultClientConfig;
import com.microsoft.graph.core.IClientConfig;
import com.microsoft.graph.extensions.GraphServiceClient;
import com.microsoft.graph.extensions.IGraphServiceClient;

import java.util.concurrent.atomic.AtomicReference;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;

public class App extends Application {

    /**
     * The service instance
     */
    private final AtomicReference<IGraphServiceClient> mClient = new AtomicReference<>();

    @Override
    public void onCreate() {
        super.onCreate();

        AndroidAudioConverter.load(this, new ILoadCallback() {
            @Override
            public void onSuccess() {
                Log.i("RecMan:App", "Android Audio Converter loaded.");
            }

            @Override
            public void onFailure(Exception error) {
                Log.e("RecMan:App", "Android Audio Converter failed to load!");
            }
        });
    }

    /**
     * Create the client configuration
     *
     * @return the newly created configuration
     */
    private IClientConfig createConfig() {
        final IAuthenticationAdapter authenticationAdapter = new MSAAuthAndroidAdapter(this) {
            @Override
            public String getClientId() {
                return "9aadc997-1173-42ca-a29e-d0b492bd2686";
            }

            @Override
            public String[] getScopes() {
                return new String[]{
                        "https://graph.microsoft.com/Files.ReadWrite",
                        "openid"
                };
            }
        };

        final IClientConfig config = DefaultClientConfig.createWithAuthenticationProvider(authenticationAdapter);
        return config;
    }


    /**
     * Get an instance of the service
     *
     * @return The Service
     */
    synchronized IGraphServiceClient getGraphServiceClient() {
        if (mClient.get() == null) {
            throw new UnsupportedOperationException("Unable to generate a new service object");
        }
        return mClient.get();
    }

    /**
     * Used to setup the Services
     */
    synchronized void createGraphServiceClient() {
        final IGraphServiceClient client = new GraphServiceClient.Builder()
                .fromConfig(createConfig())
                .buildClient();
        mClient.set(client);
    }

}