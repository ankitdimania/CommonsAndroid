package com.grootcode.android.sync;

import android.accounts.Account;
import android.content.ContentResolver;
import android.os.Bundle;

import com.grootcode.android.provider.GrootCodeContractBase;

public class SyncHelper {

    public static void requestManualSync(Account mChosenAccount) {
        Bundle b = new Bundle();
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(mChosenAccount, GrootCodeContractBase.CONTENT_AUTHORITY, b);
    }
}
