package de.tum.in.tumcampusapp.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.MainActivity;
import de.tum.in.tumcampusapp.activities.PlansDetailsActivity;
import de.tum.in.tumcampusapp.adapters.PlanListAdapter;
import de.tum.in.tumcampusapp.adapters.PlanListAdapter.PlanListEntry;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;


/**
 * Created by Thomas-Notebook on 14.05.2017.
 */

public class PlansViewFragment extends Fragment {

    private View fragmentView;
    private enum PlanFile{
        SCHNELLBAHNNETZ("http://www.mvv-muenchen.de/fileadmin/media/Dateien/plaene/pdf/Netz_2016_Version_MVG.PDF", "Schnellbahnnetz.pdf"),
        NACHTLINIENNETZ("http://www.mvv-muenchen.de/fileadmin/media/Dateien/plaene/pdf/Nachtnetz_2016.pdf", "Nachtliniennetz.pdf"),
        TRAMNETZ("http://www.mvv-muenchen.de/fileadmin/media/Dateien/plaene/pdf/Tramnetz_2016.pdf", "Tramnetz.pdf"),
        TARIFPLAN("http://www.mvv-muenchen.de/fileadmin/media/Dateien/3_Tickets_Preise/dokumente/TARIFPLAN_2016-Innenraum.pdf", "Tarifplan.pdf");

        private final String localName;
        private final String url;
        PlanFile(String url, String localName){
            this.url = url;
            this.localName = localName;
        }
        public String getLocalName(){
           return this.localName;
        }
        public String getUrl(){
            return this.url;
        }
    }

    private String fileDirectory;
    private PlanListAdapter mListAdapter;
    private ListView list;

    private ProgressBar progressBar;

    private final AsyncTask<PlanFile, Integer, Void> pdfDownloader = new AsyncTask<PlanFile, Integer, Void>() {
        @Override
        protected Void doInBackground(PlanFile... files) {
            Utils.log("Starting download.");
            NetUtils netUtils = new NetUtils(getContext().getApplicationContext());
            int progressPerFile = 100/files.length;
            int i=0;
            for (PlanFile file : files) {
                try {
                    String localFile = fileDirectory + '/' + file.getLocalName();
                    netUtils.downloadToFile(file.getUrl(), localFile);
                    publishProgress((++i)*progressPerFile);
                    Utils.log(localFile);
                } catch (IOException e) {
                    Utils.log(e);
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void v) {
            progressBar.setVisibility(View.GONE);
            list.setEnabled(true);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fileDirectory = getContext().getApplicationContext().getFilesDir().getPath();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.fragmentView = inflater.inflate(R.layout.fragment_plans_view, container, false);
        progressBar = (ProgressBar) fragmentView.findViewById(R.id.progressBar2);

        list = (ListView) fragmentView.findViewById(R.id.activity_plans_list_view);
        List<PlanListEntry> listMenuEntrySet = ImmutableList.<PlanListEntry>builder()
                .add(new PlanListEntry(R.drawable.plan_mvv_icon, R.string.mvv_fast_train_net, R.string.empty_string, 0))
                .add(new PlanListEntry(R.drawable.plan_mvv_night_icon, R.string.mvv_nightlines, R.string.empty_string, 0))
                .add(new PlanListEntry(R.drawable.plan_tram_icon, R.string.mvv_tram, R.string.empty_string, 0))
                .add(new PlanListEntry(R.drawable.mvv_entire_net_icon, R.string.mvv_entire_net, R.string.empty_string, 0))
                .add(new PlanListEntry(R.drawable.plan_campus_garching_icon, R.string.campus_garching, R.string.campus_garching_adress, R.drawable.campus_garching))
                .add(new PlanListEntry(R.drawable.plan_campus_klinikum_icon, R.string.campus_klinikum, R.string.campus_klinikum_adress, R.drawable.campus_klinikum))
                .add(new PlanListEntry(R.drawable.plan_campus_olympiapark_icon, R.string.campus_olympiapark, R.string.campus_olympiapark_adress, R.drawable.campus_olympiapark))
                .add(new PlanListEntry(R.drawable.plan_campus_olympiapark_hallenplan_icon, R.string.campus_olympiapark_gyms, R.string.campus_olympiapark_adress, R.drawable.campus_olympiapark_hallenplan))
                .add(new PlanListEntry(R.drawable.plan_campus_stammgelaende__icon, R.string.campus_main, R.string.campus_main_adress, R.drawable.campus_stammgelaende))
                .add(new PlanListEntry(R.drawable.plan_campus_weihenstephan_icon, R.string.campus_weihenstephan, R.string.campus_weihenstephan_adress, R.drawable.campus_weihenstephan))
                .build();

        mListAdapter = new PlanListAdapter(getActivity(), listMenuEntrySet);
        downloadFiles();
        list.setAdapter(mListAdapter);
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                PlanListEntry entry = (PlanListEntry) mListAdapter.getItem(pos);
                if (pos <= 3) {
                    String currentLocalName = PlanFile.values()[pos].getLocalName();
                    File pdfFile = new File(fileDirectory, currentLocalName);
                    if (pdfFile.exists()){
                        if (!openPdfViewer(pdfFile)){
                            Toast.makeText(getContext(), "Invalid file format, contact the administrator via email and attach filename.", Toast.LENGTH_LONG).show();
                        }
                    }else{
                        Toast.makeText(getContext(), "File doesn't exist yet...did you download it?", Toast.LENGTH_LONG).show();
                        downloadFiles();
                    }
                } else {
                    Intent intent = new Intent(getContext(), PlansDetailsActivity.class);
                    intent.putExtra(PlansDetailsActivity.PLAN_TITLE_ID, entry.titleId);
                    intent.putExtra(PlansDetailsActivity.PLAN_IMG_ID, entry.imgId);
                    startActivity(intent);
                }
            }
        });
        return fragmentView;
    }

    private void downloadFiles(){
        for (PlanFile file : PlanFile.values()){
            if (!(new File(fileDirectory+"/"+file.getLocalName())).exists()){
                displayDownloadDialog();
                break;
            }
        }
    }

    private void displayDownloadDialog(){
        final Intent back_intent = new Intent(getContext(), MainActivity.class);
        new AlertDialog.Builder(getContext())
            .setTitle("MVV plans")
            .setMessage(getResources().getString(R.string.mvv_download))
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(back_intent);
                }
            })
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    progressBar.setVisibility(View.VISIBLE);
                    pdfDownloader.execute(PlanFile.values());
                    list.setEnabled(false);
                }
            }).show();
    }


    public boolean openPdfViewer(File pdf){
        PdfViewFragment pdfFragment = (PdfViewFragment)getActivity().getSupportFragmentManager().findFragmentByTag("PDF_FRAGMENT");
        if (pdfFragment == null) pdfFragment = new PdfViewFragment();
        if (!pdfFragment.setPdf(pdf)) return false;
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activity_plans_fragment_frame, pdfFragment, "PDF_FRAGMENT");
        transaction.addToBackStack(null);
        transaction.commit();
        return true;
    }
}

