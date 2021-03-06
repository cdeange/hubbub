package com.deange.githubstatus.model;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import org.joda.time.LocalDateTime;


@AutoValue
public abstract class CurrentStatus
    implements
    Parcelable {

  public static CurrentStatus create(State state, LocalDateTime time) {
    return new AutoValue_CurrentStatus(state, time);
  }

  public static CurrentStatus error() {
    return new AutoValue_CurrentStatus(State.ERROR, LocalDateTime.now());
  }

  @SerializedName("status")
  public abstract State state();

  @SerializedName("last_updated")
  public abstract LocalDateTime updatedAt();

  @Memoized
  public long id() {
    return updatedAt().toDate().getTime();
  }

  public static TypeAdapter<CurrentStatus> typeAdapter(Gson gson) {
    return new AutoValue_CurrentStatus.GsonTypeAdapter(gson);
  }

}
