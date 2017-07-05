package com.smartbeings.assignment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import co.ceryle.radiorealbutton.RadioRealButton;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment implements OnMapReadyCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    MapView mMapView;
    listings listings;
    appList appList;
    TextView list_txt,map_txt;
    RadioRealButtonGroup groupType;
    String groupTypeSelect;
    GoogleMap mapgoogle;

    private OnFragmentInteractionListener mListener;
    private BitmapDescriptor mapicon;

    public SearchFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(appList appList) {
        SearchFragment fragment = new SearchFragment();
//        Bundle args = new Bundle();
//        args.putSerializable("datalist", (Serializable) appList);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        appList = (appList) getArguments().getSerializable("datalist");
        appList = ((Home)getActivity()).getYourObjects();
        System.out.println("fragmwnt search list size : "+appList.getListingses().size());
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        map_txt = (TextView) view.findViewById(R.id.map_txtvw);
        list_txt = (TextView) view.findViewById(R.id.list_txtvw);


        groupType = (RadioRealButtonGroup) view.findViewById(R.id.typeGroup);
        groupType.setPosition(0);
        groupTypeSelect = "Meeting";
        map_txt.setBackground(getResources().getDrawable(R.drawable.style_map_select));
        list_txt.setBackground(getResources().getDrawable(R.drawable.style_list_unselect));
        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mapicon = BitmapDescriptorFactory.fromResource(R.drawable.mapmark);

        groupType.setOnPositionChangedListener(new RadioRealButtonGroup.OnPositionChangedListener() {
            @Override
            public void onPositionChanged(RadioRealButton button, int currentPosition, int lastPosition) {
                System.out.println("crr p - "+currentPosition+" po "+groupType.getPosition());
                switch (currentPosition)
                {
                    case 0:
                        groupTypeSelect = "Meeting";
                        break;
                    case 1:
                        groupTypeSelect = "Workspace";
                        break;
                }
                mapgoogle.clear();
                AddMarkersOnMap(groupTypeSelect);
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMapView.getMapAsync(this);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapgoogle = googleMap;
        if (mapgoogle !=null){
//            LatLng centLoctn = new LatLng(13.060805, 77.474169);
//            googleMap.addMarker(new MarkerOptions().position(centLoctn).title("IMTMA Location"));

            AddMarkersOnMap(groupTypeSelect);

            // For zooming automatically to the location of the marker
//            CameraPosition cameraPosition = new CameraPosition.Builder().target(centLoctn).zoom(17).build();
//            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        }
    }

    public void AddMarkersOnMap(String type)
    {
        Marker marker;
        List<Marker> markers = new ArrayList<>();

        for (int i=0;i<appList.getListingses().size();i++)
        {
            if (appList.getListingses().get(i).latitude != null && appList.getListingses().get(i).getActivity().equals(type))
            {
                System.out.println("marker "+i+"    "+type);
                double latitude = Double.parseDouble(appList.getListingses().get(i).getLatitude());
                double longtitude = Double.parseDouble(appList.getListingses().get(i).getLongitude());
                LatLng centLoctn = new LatLng(latitude,longtitude );
                marker = mapgoogle.addMarker(new MarkerOptions().position(centLoctn).icon(mapicon));
                markers.add(marker);
            }
        }
        if (markers.size()>0) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker m : markers) {
                builder.include(m.getPosition());
            }
            LatLngBounds bounds = builder.build();

            int padding = 60; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

            mapgoogle.animateCamera(cu);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
