package com.example.inappbillingdemo.view;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.android.vending.billing.IInAppBillingService;
import com.example.inappbillingdemo.R;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    IInAppBillingService mService;

    Button btnPurchase;
    Button btnQueryForPurchaseItem;
    Button btnConsumePurchase;
    public static final String TAG = "MainActivity";
//    To get rid of this you should clear the cache of Google play app on your android phone.
//    When we make any in app purchase are details are maintained by the Google play services.
//    So once the a product is purchased play Isolates the particular sku,to avoid duplicate purchase.
//    Note - This is actually for testing purpose.
//    Because Google play services maintains the record of all applications which have in app purchase or Oauth or any other facilities.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending"); // the package name of Google Play app.
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);


        btnPurchase = (Button)findViewById(R.id.activity_main_btn_purchase);
        btnQueryForPurchaseItem =(Button)findViewById(R.id.activity_main_btn_query_for_purchase_item);
        btnConsumePurchase = (Button)findViewById(R.id.activity_main_btn_consume_purchase);

        btnPurchase.setOnClickListener(this);
        btnQueryForPurchaseItem.setOnClickListener(this);
        btnConsumePurchase.setOnClickListener(this);

    }

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG,"onServiceConnected");
            mService = IInAppBillingService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG,"onServiceDisconnected");
            mService = null;
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"onDestroy");
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    @Override
    public void onClick(View view) {
        if(view==btnPurchase){
            try {
                Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                        "android.test.purchased", "inapp", "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");

                PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

                if(pendingIntent!=null){
                    startIntentSenderForResult(pendingIntent.getIntentSender(),
                            1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                            Integer.valueOf(0));

                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }


        }else if(view==btnQueryForPurchaseItem){
            try {
                Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);

                int response = ownedItems.getInt("RESPONSE_CODE");
                if (response == 0) {
                    ArrayList<String> ownedSkus =
                            ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                    Log.e(TAG,"inapp puchase item list"+ownedSkus);

                    ArrayList<String>  purchaseDataList =
                            ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                    ArrayList<String>  signatureList =
                            ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                    String continuationToken =
                            ownedItems.getString("INAPP_CONTINUATION_TOKEN");

                    for (int i = 0; i < purchaseDataList.size(); ++i) {
                        String purchaseData = purchaseDataList.get(i);
                        String signature = signatureList.get(i);
                        String sku = ownedSkus.get(i);

                        // do something with this purchase information
                        // e.g. display the updated list of products owned by user
                    }

                    // if continuationToken != null, call getPurchases again
                    // and pass in the token to retrieve more items
                }
                Log.e(TAG, "You have bought the ."+ownedItems+ "Excellent choice,adventurer!");

            } catch (RemoteException e) {
                e.printStackTrace();
            }


        }else if(view==btnConsumePurchase){
            new AsyncConsumePurchase().execute("");

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    String purchaseToken=jo.getString("purchaseToken");
                    Log.e(TAG,"You have bought the " + sku + ". Excellent choice, adventurer!");
                    Log.e(TAG,"You have bought the " + purchaseToken + ". Excellent choice, adventurer!");
                }
                catch (JSONException e) {
                    Log.e(TAG,"Failed to parse purchase data.");
                    e.printStackTrace();
                }
            }
        }
    }

    private class AsyncConsumePurchase extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                int response = mService.consumePurchase(3, getPackageName(), "inapp:com.example.inappbillingdemo:android.test.purchased");
                Log.e(TAG, "You have bought the " +response + " . Excellent choice,adventurer!");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return "";
        }
    }
}
