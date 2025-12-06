/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.example.samplestickerapp;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

class StickerPack implements Parcelable {
    final String identifier;
    final String name;
    final String publisher;
    final String trayImageFile;
    final String publisherEmail;
    final String publisherWebsite;
    final String privacyPolicyWebsite;
    final String licenseAgreementWebsite;
    final String imageDataVersion;
    final boolean avoidCache;
    final boolean animatedStickerPack;

    String iosAppStoreLink;
    private List<Sticker> stickers;
    private long totalSize;
    String androidPlayStoreLink;
    private boolean isWhitelisted;

    StickerPack(String identifier, String name, String publisher, String trayImageFile, String publisherEmail, String publisherWebsite, String privacyPolicyWebsite, String licenseAgreementWebsite, String imageDataVersion, boolean avoidCache, boolean animatedStickerPack) {
        this.identifier = identifier;
        this.name = name;
        this.publisher = publisher;
        this.trayImageFile = trayImageFile;
        this.publisherEmail = publisherEmail;
        this.publisherWebsite = publisherWebsite;
        this.privacyPolicyWebsite = privacyPolicyWebsite;
        this.licenseAgreementWebsite = licenseAgreementWebsite;
        this.imageDataVersion = imageDataVersion;
        this.avoidCache = avoidCache;
        this.animatedStickerPack = animatedStickerPack;
    }

    void setIsWhitelisted(boolean isWhitelisted) {
        this.isWhitelisted = isWhitelisted;
    }

    boolean getIsWhitelisted() {
        return isWhitelisted;
    }

    private StickerPack(Parcel in) {
        identifier = in.readString();
        name = in.readString();
        publisher = in.readString();
        trayImageFile = in.readString();
        publisherEmail = in.readString();
        publisherWebsite = in.readString();
        privacyPolicyWebsite = in.readString();
        licenseAgreementWebsite = in.readString();
        iosAppStoreLink = in.readString();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            stickers = in.createTypedArrayList(Sticker.CREATOR);
        } else {
            stickers = in.createTypedArrayList(Sticker.CREATOR);
        }
        totalSize = in.readLong();
        androidPlayStoreLink = in.readString();
        isWhitelisted = in.readBoolean();
        imageDataVersion = in.readString();
        avoidCache = in.readBoolean();
        animatedStickerPack = in.readBoolean();
    }

    public static final Creator<StickerPack> CREATOR = new Creator<StickerPack>() {
        @Override
        public StickerPack createFromParcel(Parcel in) {
            return new StickerPack(in);
        }

        @Override
        public StickerPack[] newArray(int size) {
            return new StickerPack[size];
        }
    };

    void setStickers(List<Sticker> stickers) {
        this.stickers = stickers;
        totalSize = 0;
        for (Sticker sticker : stickers) {
            totalSize += sticker.size;
        }
    }

    void setAndroidPlayStoreLink(String androidPlayStoreLink) {
        this.androidPlayStoreLink = androidPlayStoreLink;
    }

    void setIosAppStoreLink(String iosAppStoreLink) {
        this.iosAppStoreLink = iosAppStoreLink;
    }

    List<Sticker> getStickers() {
        return stickers;
    }

    long getTotalSize() {
        return totalSize;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(identifier);
        dest.writeString(name);
        dest.writeString(publisher);
        dest.writeString(trayImageFile);
        dest.writeString(publisherEmail);
        dest.writeString(publisherWebsite);
        dest.writeString(privacyPolicyWebsite);
        dest.writeString(licenseAgreementWebsite);
        dest.writeString(iosAppStoreLink);
        dest.writeTypedList(stickers);
        dest.writeLong(totalSize);
        dest.writeString(androidPlayStoreLink);
        dest.writeBoolean(isWhitelisted);
        dest.writeString(imageDataVersion);
        dest.writeBoolean(avoidCache);
        dest.writeBoolean(animatedStickerPack);
    }
}
