package cz.zcu.kiv.jsmahy.minesweeper.game;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.StringJoiner;

public class Tile implements Parcelable {
    private boolean mine = false;
    private boolean revealed = false;
    private boolean flagged = false;

    private int nearbyMineCount = -1;

    public Tile() {
    }

    public Tile(Parcel in) {
        mine = in.readByte() != 0;
        revealed = in.readByte() != 0;
        flagged = in.readByte() != 0;
        nearbyMineCount = in.readInt();
    }

    public static final Creator<Tile> CREATOR = new Creator<Tile>() {
        @Override
        public Tile createFromParcel(Parcel in) {
            return new Tile(in);
        }

        @Override
        public Tile[] newArray(int size) {
            return new Tile[size];
        }
    };

    public void setMine() {
        this.mine = true;
    }

    public void toggleFlag() {
        this.flagged = !this.flagged;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public boolean isMine() {
        return mine;
    }

    @Override
    @NonNull
    public String toString() {
        return new StringJoiner(", ", Tile.class.getSimpleName() + "[", "]")
                .add("mine=" + mine)
                .add("revealed=" + revealed)
                .add("nearbyMineCount=" + nearbyMineCount)
                .toString();
    }

    public int getNearbyMineCount() {
        return nearbyMineCount;
    }

    public void reveal(@IntRange(from = 0, to = 8) int nearbyMineCount) {
        this.revealed = true;
        this.nearbyMineCount = nearbyMineCount;
    }

    public boolean isRevealed() {
        return revealed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte(this.mine ? (byte) 1 : 0);
        parcel.writeByte(this.revealed ? (byte) 1 : 0);
        parcel.writeByte(this.flagged ? (byte) 1 : 0);
        parcel.writeInt(nearbyMineCount);
    }
}
