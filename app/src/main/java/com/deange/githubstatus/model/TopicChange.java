package com.deange.githubstatus.model;


import android.os.Parcelable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class TopicChange
    implements
    Parcelable {

  @SerializedName("old_topic")
  public abstract String oldTopic();

  @SerializedName("new_topic")
  public abstract String newTopic();

  public static TopicChange create(String oldTopic, String newTopic) {
    return new AutoValue_TopicChange(oldTopic, newTopic);
  }

  public static TopicChange create() {
    return create("", "");
  }

  public TopicChange pushTopic(String newTopic) {
    return create(newTopic(), newTopic);
  }

  public static TypeAdapter<TopicChange> typeAdapter(Gson gson) {
    return new AutoValue_TopicChange.GsonTypeAdapter(gson);
  }
}
