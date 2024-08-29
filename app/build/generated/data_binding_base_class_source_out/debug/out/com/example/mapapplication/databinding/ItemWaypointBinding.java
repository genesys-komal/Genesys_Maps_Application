// Generated by view binder compiler. Do not edit!
package com.example.mapapplication.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.mapapplication.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ItemWaypointBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final AppCompatImageView clearPoint;

  @NonNull
  public final LinearLayout containerNav;

  @NonNull
  public final TextView count;

  @NonNull
  public final TextView waypoint;

  private ItemWaypointBinding(@NonNull LinearLayout rootView,
      @NonNull AppCompatImageView clearPoint, @NonNull LinearLayout containerNav,
      @NonNull TextView count, @NonNull TextView waypoint) {
    this.rootView = rootView;
    this.clearPoint = clearPoint;
    this.containerNav = containerNav;
    this.count = count;
    this.waypoint = waypoint;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ItemWaypointBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ItemWaypointBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.item_waypoint, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ItemWaypointBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.clearPoint;
      AppCompatImageView clearPoint = ViewBindings.findChildViewById(rootView, id);
      if (clearPoint == null) {
        break missingId;
      }

      id = R.id.container_nav;
      LinearLayout containerNav = ViewBindings.findChildViewById(rootView, id);
      if (containerNav == null) {
        break missingId;
      }

      id = R.id.count;
      TextView count = ViewBindings.findChildViewById(rootView, id);
      if (count == null) {
        break missingId;
      }

      id = R.id.waypoint;
      TextView waypoint = ViewBindings.findChildViewById(rootView, id);
      if (waypoint == null) {
        break missingId;
      }

      return new ItemWaypointBinding((LinearLayout) rootView, clearPoint, containerNav, count,
          waypoint);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
