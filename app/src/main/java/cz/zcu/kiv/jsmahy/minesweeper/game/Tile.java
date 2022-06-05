package cz.zcu.kiv.jsmahy.minesweeper.game;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import java.util.Objects;
import java.util.StringJoiner;

public class Tile implements Parcelable {
    public static final Creator<Tile> CREATOR = new Creator<>() {
        @Override
        public Tile createFromParcel(Parcel in) {
            return new Tile(in);
        }

        @Override
        public Tile[] newArray(int size) {
            return new Tile[size];
        }
    };
    private boolean mine;
    private boolean revealed;
    private boolean wasRevealed;
    private boolean flagged;
    private boolean wasFlagged;
    private boolean clickable;
    private boolean clickedMine;
    private int nearbyMineCount;

    public Tile() {
        reset();
    }

    public Tile(Parcel in) {
        mine = in.readByte() != 0;
        revealed = in.readByte() != 0;
        flagged = in.readByte() != 0;
        clickable = in.readByte() != 0;
        clickedMine = in.readByte() != 0;
        wasRevealed = in.readByte() != 0;
        wasFlagged = in.readByte() != 0;
        nearbyMineCount = in.readInt();
    }

    public void update() {
        this.wasFlagged = this.flagged;
        this.wasRevealed = this.revealed;
    }

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
                .add("wasRevealed=" + wasRevealed)
                .add("flagged=" + flagged)
                .add("wasFlagged=" + wasFlagged)
                .add("clickable=" + clickable)
                .add("clickedMine=" + clickedMine)
                .add("nearbyMineCount=" + nearbyMineCount)
                .toString();
    }

    public boolean isClickable() {
        return clickable;
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    public int getNearbyMineCount() {
        return nearbyMineCount;
    }

    public void setClickedMine() {
        clickedMine = true;
    }

    public boolean isClickedMine() {
        return clickedMine;
    }

    public void reveal(@IntRange(from = 0, to = 8) int nearbyMineCount) {
        this.revealed = true;
        this.nearbyMineCount = nearbyMineCount;
    }

    public boolean wasRevealed() {
        return wasRevealed;
    }

    public boolean wasFlagged() {
        return wasFlagged;
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
        parcel.writeByte(this.clickable ? (byte) 1 : 0);
        parcel.writeByte(this.clickedMine ? (byte) 1 : 0);
        parcel.writeByte(this.wasRevealed ? (byte) 1 : 0);
        parcel.writeByte(this.wasFlagged ? (byte) 1 : 0);
        parcel.writeInt(nearbyMineCount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tile tile = (Tile) o;
        return mine == tile.mine && revealed == tile.revealed && wasRevealed == tile.wasRevealed && flagged == tile.flagged && wasFlagged == tile.wasFlagged && clickable == tile.clickable && clickedMine == tile.clickedMine && nearbyMineCount == tile.nearbyMineCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mine, revealed, wasRevealed, flagged, wasFlagged, clickable, clickedMine, nearbyMineCount);
    }

    public void reset() {
        revealed = false;
        wasRevealed = false;
        flagged = false;
        wasFlagged = false;
        clickable = true;
        clickedMine = false;
        nearbyMineCount = -1;
    }
}
