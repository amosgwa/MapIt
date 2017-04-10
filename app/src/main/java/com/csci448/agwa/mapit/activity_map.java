package com.csci448.agwa.mapit;

import android.support.v4.app.Fragment;
import android.os.Bundle;

public class activity_map extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return fragment_map.newInstance();
    }
}
