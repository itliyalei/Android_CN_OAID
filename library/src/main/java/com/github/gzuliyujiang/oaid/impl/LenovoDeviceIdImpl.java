/*
 * Copyright (c) 2019-2020 gzu-liyujiang <1032694760@qq.com>
 *
 * The software is licensed under the Mulan PSL v1.
 * You can use this software according to the terms and conditions of the Mulan PSL v1.
 * You may obtain a copy of Mulan PSL v1 at:
 *     http://license.coscl.org.cn/MulanPSL
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR
 * PURPOSE.
 * See the Mulan PSL v1 for more details.
 *
 */
package com.github.gzuliyujiang.oaid.impl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.github.gzuliyujiang.logger.Logger;
import com.github.gzuliyujiang.oaid.IDeviceId;
import com.github.gzuliyujiang.oaid.IGetter;
import com.zui.deviceidservice.IDeviceidInterface;

import java.lang.reflect.Method;

/**
 * Created by liyujiang on 2020/5/30
 *
 * @author 大定府羡民
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class LenovoDeviceIdImpl implements IDeviceId {
    private Context context;

    public LenovoDeviceIdImpl(Context context) {
        this.context = context;
    }

    @Override
    public boolean supportOAID() {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo("com.zui.deviceidservice", 0);
            return pi != null;
        } catch (Exception e) {
            Logger.print(e);
            return false;
        }
    }

    @Override
    public void doGet(@NonNull final IGetter getter) {
        Intent intent = new Intent();
        intent.setClassName("com.zui.deviceidservice", "com.zui.deviceidservice.DeviceidService");
        boolean isBinded = context.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Logger.print("Lenovo DeviceidService connected");
                try {
                    //IDeviceidInterface anInterface = new IDeviceidInterface.Stub.asInterface(service);
                    Method asInterface = IDeviceidInterface.Stub.class.getDeclaredMethod("asInterface", IBinder.class);
                    IDeviceidInterface anInterface = (IDeviceidInterface) asInterface.invoke(null, service);
                    String deviceId = anInterface.a();
                    if (deviceId == null || deviceId.length() == 0) {
                        getter.onDeviceIdGetError(new RuntimeException("Lenovo deviceId get failed"));
                    } else {
                        getter.onDeviceIdGetComplete(deviceId);
                    }
                } catch (Exception e) {
                    Logger.print(e);
                    getter.onDeviceIdGetError(e);
                } finally {
                    context.unbindService(this);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Logger.print("Lenovo DeviceidService disconnected");
            }
        }, Context.BIND_AUTO_CREATE);
        if (!isBinded) {
            getter.onDeviceIdGetError(new RuntimeException("Lenovo DeviceidService bind failed"));
        }
    }

}
