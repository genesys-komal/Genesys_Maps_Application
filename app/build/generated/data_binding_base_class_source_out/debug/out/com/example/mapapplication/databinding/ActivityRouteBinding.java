// Generated by view binder compiler. Do not edit!
package com.example.mapapplication.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.mapapplication.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.mapboxsdk.maps.MapView;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityRouteBinding implements ViewBinding {
  @NonNull
  private final RelativeLayout rootView;

  @NonNull
  public final LinearLayout bicycleLayout;

  @NonNull
  public final TextView bikeTime;

  @NonNull
  public final LinearLayout bottomView;

  @NonNull
  public final LinearLayout busLayout;

  @NonNull
  public final TextView busTime;

  @NonNull
  public final LinearLayout carLayout;

  @NonNull
  public final TextView carTime;

  @NonNull
  public final AppCompatImageView clearDestination;

  @NonNull
  public final AppCompatImageView clearOrigin;

  @NonNull
  public final ImageView clearPoints;

  @NonNull
  public final RelativeLayout container;

  @NonNull
  public final LinearLayout containerNav;

  @NonNull
  public final LinearLayout containerNavigation;

  @NonNull
  public final AutoCompleteTextView destinationAutocomplete;

  @NonNull
  public final TextView destinationPoint;

  @NonNull
  public final TextView distance;

  @NonNull
  public final FloatingActionButton fabRecenter;

  @NonNull
  public final FloatingActionButton fabStyles;

  @NonNull
  public final LinearLayout frame;

  @NonNull
  public final LinearLayout legendContainer;

  @NonNull
  public final LinearLayout mainContainer;

  @NonNull
  public final MapView mapView;

  @NonNull
  public final TextView optimalTime;

  @NonNull
  public final AutoCompleteTextView originAutocomplete;

  @NonNull
  public final TextView originPoint;

  @NonNull
  public final RecyclerView recyclerViewSearch;

  @NonNull
  public final RecyclerView recyclerWayPoints;

  @NonNull
  public final ImageView shuffle;

  @NonNull
  public final TextView startRouteButton;

  @NonNull
  public final FloatingActionButton startRouteButton1;

  @NonNull
  public final LinearLayout startRouteLayout;

  @NonNull
  public final TextView time;

  @NonNull
  public final LinearLayout timeLyt;

  @NonNull
  public final LinearLayout truckLayout;

  @NonNull
  public final TextView truckTime;

  @NonNull
  public final LinearLayout walkLayout;

  @NonNull
  public final TextView walkTime;

  private ActivityRouteBinding(@NonNull RelativeLayout rootView,
      @NonNull LinearLayout bicycleLayout, @NonNull TextView bikeTime,
      @NonNull LinearLayout bottomView, @NonNull LinearLayout busLayout, @NonNull TextView busTime,
      @NonNull LinearLayout carLayout, @NonNull TextView carTime,
      @NonNull AppCompatImageView clearDestination, @NonNull AppCompatImageView clearOrigin,
      @NonNull ImageView clearPoints, @NonNull RelativeLayout container,
      @NonNull LinearLayout containerNav, @NonNull LinearLayout containerNavigation,
      @NonNull AutoCompleteTextView destinationAutocomplete, @NonNull TextView destinationPoint,
      @NonNull TextView distance, @NonNull FloatingActionButton fabRecenter,
      @NonNull FloatingActionButton fabStyles, @NonNull LinearLayout frame,
      @NonNull LinearLayout legendContainer, @NonNull LinearLayout mainContainer,
      @NonNull MapView mapView, @NonNull TextView optimalTime,
      @NonNull AutoCompleteTextView originAutocomplete, @NonNull TextView originPoint,
      @NonNull RecyclerView recyclerViewSearch, @NonNull RecyclerView recyclerWayPoints,
      @NonNull ImageView shuffle, @NonNull TextView startRouteButton,
      @NonNull FloatingActionButton startRouteButton1, @NonNull LinearLayout startRouteLayout,
      @NonNull TextView time, @NonNull LinearLayout timeLyt, @NonNull LinearLayout truckLayout,
      @NonNull TextView truckTime, @NonNull LinearLayout walkLayout, @NonNull TextView walkTime) {
    this.rootView = rootView;
    this.bicycleLayout = bicycleLayout;
    this.bikeTime = bikeTime;
    this.bottomView = bottomView;
    this.busLayout = busLayout;
    this.busTime = busTime;
    this.carLayout = carLayout;
    this.carTime = carTime;
    this.clearDestination = clearDestination;
    this.clearOrigin = clearOrigin;
    this.clearPoints = clearPoints;
    this.container = container;
    this.containerNav = containerNav;
    this.containerNavigation = containerNavigation;
    this.destinationAutocomplete = destinationAutocomplete;
    this.destinationPoint = destinationPoint;
    this.distance = distance;
    this.fabRecenter = fabRecenter;
    this.fabStyles = fabStyles;
    this.frame = frame;
    this.legendContainer = legendContainer;
    this.mainContainer = mainContainer;
    this.mapView = mapView;
    this.optimalTime = optimalTime;
    this.originAutocomplete = originAutocomplete;
    this.originPoint = originPoint;
    this.recyclerViewSearch = recyclerViewSearch;
    this.recyclerWayPoints = recyclerWayPoints;
    this.shuffle = shuffle;
    this.startRouteButton = startRouteButton;
    this.startRouteButton1 = startRouteButton1;
    this.startRouteLayout = startRouteLayout;
    this.time = time;
    this.timeLyt = timeLyt;
    this.truckLayout = truckLayout;
    this.truckTime = truckTime;
    this.walkLayout = walkLayout;
    this.walkTime = walkTime;
  }

  @Override
  @NonNull
  public RelativeLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityRouteBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityRouteBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_route, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityRouteBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.bicycleLayout;
      LinearLayout bicycleLayout = ViewBindings.findChildViewById(rootView, id);
      if (bicycleLayout == null) {
        break missingId;
      }

      id = R.id.bikeTime;
      TextView bikeTime = ViewBindings.findChildViewById(rootView, id);
      if (bikeTime == null) {
        break missingId;
      }

      id = R.id.bottomView;
      LinearLayout bottomView = ViewBindings.findChildViewById(rootView, id);
      if (bottomView == null) {
        break missingId;
      }

      id = R.id.busLayout;
      LinearLayout busLayout = ViewBindings.findChildViewById(rootView, id);
      if (busLayout == null) {
        break missingId;
      }

      id = R.id.busTime;
      TextView busTime = ViewBindings.findChildViewById(rootView, id);
      if (busTime == null) {
        break missingId;
      }

      id = R.id.carLayout;
      LinearLayout carLayout = ViewBindings.findChildViewById(rootView, id);
      if (carLayout == null) {
        break missingId;
      }

      id = R.id.carTime;
      TextView carTime = ViewBindings.findChildViewById(rootView, id);
      if (carTime == null) {
        break missingId;
      }

      id = R.id.clearDestination;
      AppCompatImageView clearDestination = ViewBindings.findChildViewById(rootView, id);
      if (clearDestination == null) {
        break missingId;
      }

      id = R.id.clearOrigin;
      AppCompatImageView clearOrigin = ViewBindings.findChildViewById(rootView, id);
      if (clearOrigin == null) {
        break missingId;
      }

      id = R.id.clearPoints;
      ImageView clearPoints = ViewBindings.findChildViewById(rootView, id);
      if (clearPoints == null) {
        break missingId;
      }

      RelativeLayout container = (RelativeLayout) rootView;

      id = R.id.container_nav;
      LinearLayout containerNav = ViewBindings.findChildViewById(rootView, id);
      if (containerNav == null) {
        break missingId;
      }

      id = R.id.container_navigation;
      LinearLayout containerNavigation = ViewBindings.findChildViewById(rootView, id);
      if (containerNavigation == null) {
        break missingId;
      }

      id = R.id.destinationAutocomplete;
      AutoCompleteTextView destinationAutocomplete = ViewBindings.findChildViewById(rootView, id);
      if (destinationAutocomplete == null) {
        break missingId;
      }

      id = R.id.destinationPoint;
      TextView destinationPoint = ViewBindings.findChildViewById(rootView, id);
      if (destinationPoint == null) {
        break missingId;
      }

      id = R.id.distance;
      TextView distance = ViewBindings.findChildViewById(rootView, id);
      if (distance == null) {
        break missingId;
      }

      id = R.id.fab_recenter;
      FloatingActionButton fabRecenter = ViewBindings.findChildViewById(rootView, id);
      if (fabRecenter == null) {
        break missingId;
      }

      id = R.id.fab_styles;
      FloatingActionButton fabStyles = ViewBindings.findChildViewById(rootView, id);
      if (fabStyles == null) {
        break missingId;
      }

      id = R.id.frame;
      LinearLayout frame = ViewBindings.findChildViewById(rootView, id);
      if (frame == null) {
        break missingId;
      }

      id = R.id.legendContainer;
      LinearLayout legendContainer = ViewBindings.findChildViewById(rootView, id);
      if (legendContainer == null) {
        break missingId;
      }

      id = R.id.mainContainer;
      LinearLayout mainContainer = ViewBindings.findChildViewById(rootView, id);
      if (mainContainer == null) {
        break missingId;
      }

      id = R.id.mapView;
      MapView mapView = ViewBindings.findChildViewById(rootView, id);
      if (mapView == null) {
        break missingId;
      }

      id = R.id.optimalTime;
      TextView optimalTime = ViewBindings.findChildViewById(rootView, id);
      if (optimalTime == null) {
        break missingId;
      }

      id = R.id.originAutocomplete;
      AutoCompleteTextView originAutocomplete = ViewBindings.findChildViewById(rootView, id);
      if (originAutocomplete == null) {
        break missingId;
      }

      id = R.id.originPoint;
      TextView originPoint = ViewBindings.findChildViewById(rootView, id);
      if (originPoint == null) {
        break missingId;
      }

      id = R.id.recycler_view_search;
      RecyclerView recyclerViewSearch = ViewBindings.findChildViewById(rootView, id);
      if (recyclerViewSearch == null) {
        break missingId;
      }

      id = R.id.recycler_way_points;
      RecyclerView recyclerWayPoints = ViewBindings.findChildViewById(rootView, id);
      if (recyclerWayPoints == null) {
        break missingId;
      }

      id = R.id.shuffle;
      ImageView shuffle = ViewBindings.findChildViewById(rootView, id);
      if (shuffle == null) {
        break missingId;
      }

      id = R.id.startRouteButton;
      TextView startRouteButton = ViewBindings.findChildViewById(rootView, id);
      if (startRouteButton == null) {
        break missingId;
      }

      id = R.id.startRouteButton1;
      FloatingActionButton startRouteButton1 = ViewBindings.findChildViewById(rootView, id);
      if (startRouteButton1 == null) {
        break missingId;
      }

      id = R.id.startRouteLayout;
      LinearLayout startRouteLayout = ViewBindings.findChildViewById(rootView, id);
      if (startRouteLayout == null) {
        break missingId;
      }

      id = R.id.time;
      TextView time = ViewBindings.findChildViewById(rootView, id);
      if (time == null) {
        break missingId;
      }

      id = R.id.timeLyt;
      LinearLayout timeLyt = ViewBindings.findChildViewById(rootView, id);
      if (timeLyt == null) {
        break missingId;
      }

      id = R.id.truckLayout;
      LinearLayout truckLayout = ViewBindings.findChildViewById(rootView, id);
      if (truckLayout == null) {
        break missingId;
      }

      id = R.id.truckTime;
      TextView truckTime = ViewBindings.findChildViewById(rootView, id);
      if (truckTime == null) {
        break missingId;
      }

      id = R.id.walkLayout;
      LinearLayout walkLayout = ViewBindings.findChildViewById(rootView, id);
      if (walkLayout == null) {
        break missingId;
      }

      id = R.id.walkTime;
      TextView walkTime = ViewBindings.findChildViewById(rootView, id);
      if (walkTime == null) {
        break missingId;
      }

      return new ActivityRouteBinding((RelativeLayout) rootView, bicycleLayout, bikeTime,
          bottomView, busLayout, busTime, carLayout, carTime, clearDestination, clearOrigin,
          clearPoints, container, containerNav, containerNavigation, destinationAutocomplete,
          destinationPoint, distance, fabRecenter, fabStyles, frame, legendContainer, mainContainer,
          mapView, optimalTime, originAutocomplete, originPoint, recyclerViewSearch,
          recyclerWayPoints, shuffle, startRouteButton, startRouteButton1, startRouteLayout, time,
          timeLyt, truckLayout, truckTime, walkLayout, walkTime);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
