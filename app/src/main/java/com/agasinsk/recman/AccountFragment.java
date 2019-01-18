package com.agasinsk.recman;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.identity.client.AuthenticationResult;
import com.microsoft.identity.client.MsalClientException;
import com.microsoft.identity.client.MsalException;
import com.microsoft.identity.client.MsalServiceException;
import com.microsoft.identity.client.MsalUiRequiredException;
import com.microsoft.identity.client.User;

import java.util.List;

public class AccountFragment extends Fragment implements MSALAuthenticationCallback {

    private static final String TAG = "AccountFragment";

    private boolean mEnablePiiLogging = false;
    private User mUser;
    private Handler mHandler;

    private OnFragmentInteractionListener mListener;
    private View mFragmentView;

    private ProgressBar mConnectProgressBar;
    private Button mConnectButton;
    private TextView mDescriptionTextView;
    private View mTitleTextView;

    public AccountFragment() {
        // Required empty public constructor
    }

    public static AccountFragment newInstance() {
        return new AccountFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mFragmentView = inflater.inflate(R.layout.fragment_account, container, false);
        // set up our views
        mConnectButton = mFragmentView.findViewById(R.id.connectButton);
        mConnectProgressBar = mFragmentView.findViewById(R.id.connectProgressBar);
        mTitleTextView = mFragmentView.findViewById(R.id.titleTextView);
        mDescriptionTextView = mFragmentView.findViewById(R.id.descriptionTextView);

        // add click listener
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConnectingInProgressUI();
                connect();
            }
        });

        return mFragmentView;
    }

    private void showConnectingInProgressUI() {
        mConnectButton.setVisibility(View.GONE);
        mTitleTextView.setVisibility(View.GONE);
        mDescriptionTextView.setVisibility(View.GONE);
        mConnectProgressBar.setVisibility(View.VISIBLE);
    }

    private void connect() {

        AuthenticationManager authenticationManager = AuthenticationManager.getInstance(getContext());

        /* Attempt to get a user and acquireTokenSilent
         * If this fails we do an interactive request
         */
        List<User> users;
        try {
            users = authenticationManager.getPublicClient().getUsers();

            if (users != null && users.size() == 1) {
                /* We have 1 user */
                mUser = users.get(0);
                authenticationManager.callAcquireTokenSilent(
                        mUser,
                        true,
                        this);
            } else {
                /* We have no user */
                /* Let's do an interactive request */
                authenticationManager.callAcquireToken(getActivity(),
                        this);
            }
        } catch (MsalClientException e) {
            Log.d(TAG, "MSAL Exception Generated while getting users: " + e.toString());
            showConnectErrorUI(e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            Log.d(TAG, "User at this position does not exist: " + e.toString());
            showConnectErrorUI(e.getMessage());
        } catch (IllegalStateException e) {
            Log.d(TAG, "MSAL Exception Generated: " + e.toString());
            showConnectErrorUI(e.getMessage());
        } catch (Exception e) {
            String errorText = getResources().getString(R.string.title_text_error);
            showConnectErrorUI(errorText);
        }
    }

    private void showConnectErrorUI(String errorMessage) {
        mConnectButton.setVisibility(View.VISIBLE);
        mConnectProgressBar.setVisibility(View.GONE);

        mDescriptionTextView.setText(errorMessage);
        mDescriptionTextView.setVisibility(View.VISIBLE);
        Toast.makeText(getContext(),
                R.string.toast_connect_error,
                Toast.LENGTH_LONG).show();
    }

    private void showMessage(final String msg) {
        getHandler().post(() -> Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show());
    }

    private Handler getHandler() {
        if (mHandler == null) {
            return new Handler(getActivity().getMainLooper());
        }

        return mHandler;
    }

    @Override
    public void onSuccess(AuthenticationResult authenticationResult) {
        mUser = authenticationResult.getUser();

        String name = "";
        String preferredUsername = "";

        try {
            // get the user info from the id token
            name = authenticationResult.getUser().getName();
            preferredUsername = authenticationResult.getUser().getDisplayableId();
            Log.i(TAG, "Retrieved user name: " + name + " and id: " + preferredUsername);

        } catch (NullPointerException npe) {
            Log.e(TAG, npe.getMessage());
        }
        // TODO: send name to mainActivity

        new Thread(() -> resetUIForConnect());
    }

    private void resetUIForConnect() {
        mConnectButton.setVisibility(View.VISIBLE);
        mTitleTextView.setVisibility(View.GONE);
        mDescriptionTextView.setVisibility(View.GONE);
        mConnectProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onError(MsalException exception) {
        // Check the exception type.
        if (exception instanceof MsalClientException) {
            // This means errors happened in the sdk itself, could be network, Json parse, etc. Check MsalError.java
            // for detailed list of the errors.
            showMessage(exception.getMessage());
            showConnectErrorUI(exception.getMessage());
        } else if (exception instanceof MsalServiceException) {
            // This means something is wrong when the sdk is communication to the service, mostly likely it's the client
            // configuration.
            showMessage(exception.getMessage());
            showConnectErrorUI(exception.getMessage());
        } else if (exception instanceof MsalUiRequiredException) {
            // This explicitly indicates that developer needs to prompt the user, it could be refresh token is expired, revoked
            // or user changes the password; or it could be that no token was found in the token cache.
            AuthenticationManager mgr = AuthenticationManager.getInstance(getContext());

            mgr.callAcquireToken(getActivity(), this);
        }
    }

    @Override
    public void onError(Exception exception) {
        showMessage(exception.getMessage());
        showConnectErrorUI(exception.getMessage());
    }

    @Override
    public void onCancel() {
        showMessage("User cancelled the flow.");
        showConnectErrorUI("User cancelled the flow.");
    }

    public interface OnFragmentInteractionListener {

    }
}