package io.icode.concareghadmin.application.activities.Notifications;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

@SuppressWarnings("ALL")
public class MyFirebaseIdService extends FirebaseInstanceIdService{

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        FirebaseUser currentAdmin = FirebaseAuth.getInstance().getCurrentUser();

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        if(currentAdmin != null){
            updateToken(refreshedToken);
        }

    }

    // Refreshes current Admin Token
    private void updateToken(String refreshedToken) {

        FirebaseUser currentAdmin = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(refreshedToken);
        assert currentAdmin != null;
        reference.child(currentAdmin.getUid()).setValue(token);

    }
}
