package com.stardust.scriptdroid.tile;

import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.annotation.RequiresApi;

import com.stardust.scriptdroid.droid.assist.Assistant;

/**
 * Created by Stardust on 2017/1/26.
 */

@RequiresApi(api = Build.VERSION_CODES.N)
public class BoundsAssistEnableTileService extends TileService {

    public void onClick() {
        Assistant.setAssistModeEnable(!Assistant.isAssistModeEnable());
        updateTile();
    }

    private void updateTile() {
        getQsTile().setState(Assistant.isAssistModeEnable() ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        getQsTile().updateTile();
    }


    @Override
    public void onStartListening() {
        updateTile();
    }
}