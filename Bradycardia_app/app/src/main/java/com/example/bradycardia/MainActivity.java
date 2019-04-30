package com.example.bradycardia;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Spinner;
import android.widget.Button;
import android.widget.AdapterView;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.internal.Utils;
import java.util.Collections;
import java.util.Comparator;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;



public class MainActivity extends AppCompatActivity {

    @BindView(R.id.patient_spinner)
    Spinner patient_spinner;

    @BindView(R.id.model_spinner)
    Spinner model_spinner;

    @BindView(R.id.detect_button)
    Button detect_button;

    @BindView(R.id.predict_button)
    Button predict_button;

    String patient;
    String model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //Button classify_button = (Button)findViewById(R.id.classify_button);
        //Button predict_button = (Button)findViewById(R.id.predict_button);

        patient_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String text = patient_spinner.getSelectedItem().toString();
                Log.d("PatientSpinner:", text);
                patient = text;
            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        model_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String text = model_spinner.getSelectedItem().toString();
                Log.d("ModelSpinner:", text);
                model = text;
            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        detect_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Detect:", "Button clicked");
                Detect(patient, model);
            }
        });

        predict_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Predict:", "Button clicked");
                Predict(patient, model);
            }
        });
    }

    private ArrayList<Integer> getheartrates(String filename)
    {

        String line;
        ArrayList<Integer> heartrates = new ArrayList<Integer>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open(filename)));

            while ((line = reader.readLine()) != null) {
                heartrates.add(Integer.parseInt(line));

            }
        }
        catch (IOException e)
        {

        }

        Log.d("FileRead:",heartrates.toString());
        return heartrates;
    }

    private  int predictSVM(List<Integer> heartrates)
    {
        int predict = 0;
        int y;
        for(int i =0 ;i < heartrates.size();i++)
        {
            y = (-2 * heartrates.get(i)) + 119;
            if (y >= 1)
            {
                predict = 1;
                break;
            }
        }
        return predict;
    }


    private  int predictKNN(List<Integer> training, List<Integer> tr_labels, List<Integer> test)
    {

        int predict = 0;

        int tr;
        int te;
        ArrayList<Integer> x = null;
        for (int j=0; j < test.size();j++)
        {
            te = test.get(j);
            ArrayList<ArrayList<Integer>> distance_list = new ArrayList<ArrayList<Integer>>();
            for (int i = 0; i < training.size(); i++) {
                tr = training.get(i);
                x = new ArrayList<Integer>();
                x.add(Math.abs(tr - te));
                x.add(tr_labels.get(i));

                distance_list.add(x);
            }

            Collections.sort(distance_list, new Comparator<ArrayList<Integer>>()
            {
                public int compare(ArrayList<Integer> p1, ArrayList<Integer> p2) {
                    return p1.get(0).compareTo(p2.get(0));

                }
            });

            Collections.reverse(distance_list);
            Log.d("Predict:",distance_list.toString() );
            int num_brady = 0;
            int num_non = 0;
            for (int i =0; i<4; i++)
            {
                x = distance_list.get(i);
                if (x.get(1) == 1)
                {
                    num_brady++;
                }
                else
                {
                    num_non ++;
                }
            }

            if(num_brady > num_non)
            {
                predict =1;
                break;
            }
        }



        return predict;
    }


    public void Detect(String patient, String model)
    {
        ArrayList<Integer> heartrates = null;
        float avg;
        int fp=0, fn=0, tn=0, tp=0;
        int i;
        int brady_count = 0;
        Log.d("Detect:", patient);
        if (patient.equals("Patient 1"))
            Log.d("Detect:","FOUND");

        long startTime = System.currentTimeMillis();
        switch(patient) {
            case ("Patient 1"): {

                heartrates = getheartrates("Patient_272.csv");
                break;
            }
            case ("Patient 2"): {
                heartrates = getheartrates("Patient_273.csv");
                break;
            }
            case ("Patient 3"): {
                heartrates = getheartrates("Patient_420.csv");
                break;
            }
            case ("Patient 4"): {
                heartrates = getheartrates("Patient_483.csv");
                break;
            }
        }

        if (heartrates != null && heartrates.size() > 0)
        {
            for (i=0; i < heartrates.size() - 3; i++)
            {
                avg = (heartrates.get(i) + heartrates.get(i+1) + heartrates.get(i+3))/3;

                if (avg < 60 && heartrates.get(i) >= 60) {
                    fp = fp + 1;
                }
                if (avg > 60 && heartrates.get(i) <= 60) {
                    fn = fn + 1;
                }
                if (avg < 60 && heartrates.get(i) < 60) {
                    brady_count++;
                    tn = tn + 1;
                }
                if (avg > 60 && heartrates.get(i) > 60) {

                    tp = tp + 1;
                }
            }
        }

        if (brady_count > 0)
        {
            Log.d("Detect:","Brady Detected");
            Log.d("Detect:","fp:" + Integer.toString(fp) + "fn:" + Integer.toString(fn)
                            + "tp:" + Integer.toString(tp) + "tn:" + Integer.toString(tn));
        }
        else
        {
            Log.d("Detect:","Brady NOT Detected");
        }

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        Log.d("Detect:",Integer.toString((int)elapsedTime));
    }

    public void Predict(String patient, String model)
    {
        ArrayList<Integer> heartrates = null;
        ArrayList<Integer> labels = null;
        List<Integer> test = null;
        int predict = 0;
        switch(patient) {
            case ("Patient 1"): {

                heartrates = getheartrates("Patient_272.csv");
                labels = getheartrates("Labels_272.csv");
                test = heartrates.subList(20,29);
                break;
            }
            case ("Patient 2"): {
                heartrates = getheartrates("Patient_273.csv");
                labels = getheartrates("Labels_273.csv");
                test = heartrates.subList(20,29);
                break;
            }
            case ("Patient 3"): {
                heartrates = getheartrates("Patient_420.csv");
                labels = getheartrates("Labels_420.csv");
                test = heartrates.subList(20,29);
                break;
            }
            case ("Patient 4"): {
                heartrates = getheartrates("Patient_483.csv");
                labels = getheartrates("Labels_483.csv");
                test = heartrates.subList(20,29);
                break;
            }
        }

        if(heartrates !=null && test != null && labels !=null)
        {
            switch (model)
            {
                case("SVM"):
                {
                    predict = predictSVM(test);
                    break;
                }
                case("K-Nearest Neighbor"):
                {
                    predict = predictKNN(heartrates.subList(0,19), labels.subList(0,19), test);
                    break;
                }


            }
        }

        if (predict == 1)
            Log.d("Predict:", "Brady Predicted");
        else
            Log.d("Predict:", "Brady NOT Predicted");

    }
}
