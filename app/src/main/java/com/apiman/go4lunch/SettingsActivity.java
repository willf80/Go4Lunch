package com.apiman.go4lunch;

import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;

import com.apiman.go4lunch.helpers.NotificationHelper;
import com.apiman.go4lunch.helpers.SettingsHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

public class SettingsActivity extends BaseActivity {

    @BindView(R.id.notificationSwitch)
    Switch notificationSwitch;

    @BindView(R.id.notificationHelpTextView)
    TextView notificationHelpTextView;

    NotificationHelper mNotificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        displayHomeAsUp();

        mNotificationHelper = new NotificationHelper(this);

        boolean initialStatus = SettingsHelper.isNotificationEnabled(this);
        notificationSwitch.setChecked(initialStatus);
        initView(initialStatus);
    }

    @OnCheckedChanged(R.id.notificationSwitch)
    void onSwitchCheckedChange(boolean isChecked) {
        SettingsHelper.saveNotificationStatus(this, isChecked);
        initView(isChecked);

        //Handle notification
        handleNotification(isChecked);
    }

    private void handleNotification(boolean status) {
        if(status) {
            mNotificationHelper.startAlarm();
        }
        else {
            mNotificationHelper.stopAlarm();
        }
    }

    private void initView(boolean status) {
        if(status) {
            notificationHelpTextView.setText(R.string.notification_enable_help_text);
            notificationSwitch.setText(R.string.notification_is_enable);
        }else {
            notificationHelpTextView.setText(R.string.notification_disable_help_text);
            notificationSwitch.setText(R.string.notification_is_disable);
        }
    }
}
