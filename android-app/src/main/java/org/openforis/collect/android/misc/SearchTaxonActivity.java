package org.openforis.collect.android.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openforis.collect.android.R;
import org.openforis.collect.android.fields.TaxonField;
import org.openforis.collect.android.lists.DownloadActivity;
import org.openforis.collect.android.management.ApplicationManager;
import org.openforis.collect.android.management.TaxonManager;
import org.openforis.collect.persistence.TaxonDao;
import org.openforis.collect.persistence.TaxonVernacularNameDao;
import org.openforis.collect.persistence.TaxonomyDao;
import org.openforis.idm.model.TaxonOccurrence;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.method.QwertyKeyListener;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class SearchTaxonActivity extends Activity {

	private String content;
	private String criteria;
	private String path;
	private int taxonFieldId;
	private TaxonManager taxonManager;
	private String taxonomy;
	private int backgroundColor;
	//UI elements
	private ListView lstResult;
	private TextView lblSearch;
	private EditText txtSearch;
	private Button btnSearch;
		
	private ProgressDialog pd;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    getWindow().requestFeature(Window.FEATURE_OPTIONS_PANEL);
	    Log.i(getResources().getString(R.string.app_name), "SearchTaxon activity started");
	    Bundle extras = getIntent().getExtras(); 
	    setContentView(R.layout.searchtaxon);
		//Add UI
	    this.lblSearch = (TextView)findViewById(R.id.lblSearch);
	    this.txtSearch = (EditText)findViewById(R.id.txtSearch);
	    this.btnSearch = (Button)findViewById(R.id.btnSearch);
		this.lstResult = (ListView)findViewById(R.id.lstResult);
		
	    if (extras != null) {
	    	//get extras
	    	this.content = extras.getString("content");
	    	this.criteria = extras.getString("criteria");  
	    	this.taxonFieldId = extras.getInt("taxonId");
	    	this.path = extras.getString("path");
	    	//Set up species manager
			this.taxonManager = new TaxonManager();
			this.taxonManager.setTaxonomyDao(new TaxonomyDao());
			this.taxonManager.setTaxonDao(new TaxonDao());
			this.taxonManager.setTaxonVernacularNameDao(new TaxonVernacularNameDao());
			this.taxonManager.setSurveyId(ApplicationManager.getSurvey().getId());
			this.taxonomy = "trees";	  
	    }
	    else{
	    	Log.i(getResources().getString(R.string.app_name), "Cannot get extras in SearchTaxon activity");
	    }
	}
	
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(getResources().getString(R.string.app_name), "SearchTaxon activity onResume");
        // The activity has become visible (it is now "resumed").
    	Log.i(getResources().getString(R.string.app_name), "Content is: " + this.content);
    	Log.i(getResources().getString(R.string.app_name), "Criteria is: " + this.criteria);
    	// Set background color
		this.backgroundColor = ApplicationManager.appPreferences.getInt(getResources().getString(R.string.backgroundColor), Color.WHITE);		
		changeBackgroundColor(this.backgroundColor);
		
		this.lblSearch.setText("Search by " + this.criteria);
		// Set value to search text box
		this.txtSearch.setText(this.content);
		// Set onFocus listener for Search texbox
		this.txtSearch.setOnFocusChangeListener(new OnFocusChangeListener() {
		    @Override
		    public void onFocusChange(View v, boolean hasFocus) {
		    	// Get current settings about software keyboard for text fields
		    	if(hasFocus){
				    	Map<String, ?> settings = ApplicationManager.appPreferences.getAll();
				    	Boolean valueForText = (Boolean)settings.get(getResources().getString(R.string.showSoftKeyboardOnTextField));
				    	// Switch on or off Software keyboard depend of settings
				    	if(valueForText){
				    		Log.i(getResources().getString(R.string.app_name), "From ClickListener: Setting search field is: " + valueForText);
				    		txtSearch.setKeyListener(new QwertyKeyListener(TextKeyListener.Capitalize.NONE, false));
				        }
				    	else {
				    		Log.i(getResources().getString(R.string.app_name), "From ClickListener: Setting search field is: " + valueForText);
				    		txtSearch.setInputType(InputType.TYPE_NULL);
				    	}
		    	}
		    }
	    });
		
		//When user click inside txtSearch
		this.txtSearch.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
		    	Map<String, ?> settings = ApplicationManager.appPreferences.getAll();
		    	Boolean valueForText = (Boolean)settings.get(getResources().getString(R.string.showSoftKeyboardOnTextField));
		    	// Switch on or off Software keyboard depend of settings
		    	if(valueForText){
		    		Log.i(getResources().getString(R.string.app_name), "From ClickListener: Setting search field is: " + valueForText);
		    		txtSearch.setKeyListener(new QwertyKeyListener(TextKeyListener.Capitalize.NONE, false));
		        }
		    	else {
		    		Log.i(getResources().getString(R.string.app_name), "From ClickListener: Setting search field is: " + valueForText);
		    		txtSearch.setInputType(InputType.TYPE_NULL);
		    	}
			}			
		});		
		
		this.btnSearch.setText("Search");
		// Add click listener for button Search
		this.btnSearch.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Log.i(getResources().getString(R.string.app_name), "Search started");
				doSearch(txtSearch.getText().toString(), taxonFieldId);
				
			}});
//		this.doSearch(this.txtSearch.getText().toString(), this.taxonFieldId);	
    }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	    	Log.i(getResources().getString(R.string.app_name), "Button BACK pressed from SearchTaxon activity");
		    //Finish activity  	
		    finish();  	
	    }
	    return super.onKeyDown(keyCode, event);
	}
	    
    private void changeBackgroundColor(int backgroundColor){
		getWindow().setBackgroundDrawable(new ColorDrawable(backgroundColor));
		int color = (backgroundColor!=Color.WHITE)?Color.WHITE:Color.BLACK;
		//Set text color
		this.lblSearch.setTextColor(color);
		this.txtSearch.setTextColor(color);
		this.btnSearch.setTextColor(color);
    }	
    
    private void doSearch(String strSearch, int parentTaxonFieldId){
    	pd = ProgressDialog.show(SearchTaxonActivity.this, getResources().getString(R.string.workInProgress), getResources().getString(R.string.searchingForTaxon));
    	SearchThread searchThread = new SearchThread(strSearch, parentTaxonFieldId);
    	searchThread.start();
    }
    
   
    
    private class SearchThread extends Thread {

    	private List<TaxonOccurrence> lstTaxonOccurence;
    	private String strSearch;
    	private int parentTaxonFieldId;
    	
        public SearchThread(String strSearch, int parentTaxonFieldId) {
            this.strSearch = strSearch;
            this.parentTaxonFieldId = parentTaxonFieldId;
        }

        @Override
        public void run() {         
        	//Open connection with database
        	JdbcDaoSupport jdbcDao  = new JdbcDaoSupport();
        	jdbcDao.getConnection();    	
        	//Search results 
        	this.lstTaxonOccurence = new ArrayList<TaxonOccurrence>();
        	if(SearchTaxonActivity.this.taxonManager != null){
        		Log.i(getResources().getString(R.string.app_name), "Search by: " + SearchTaxonActivity.this.criteria);
        		    		
        		if(SearchTaxonActivity.this.criteria.equalsIgnoreCase("Code")){
    				Log.i(getResources().getString(R.string.app_name), "Search by Code");
    				lstTaxonOccurence = SearchTaxonActivity.this.taxonManager.findByCode(SearchTaxonActivity.this.taxonomy, strSearch, 1000);			
    			}
    			else if (SearchTaxonActivity.this.criteria.equalsIgnoreCase("SciName")){
    				Log.i(getResources().getString(R.string.app_name), "Search by Scientific name");
    				lstTaxonOccurence = SearchTaxonActivity.this.taxonManager.findByScientificName(SearchTaxonActivity.this.taxonomy, strSearch, 1000);		
    			}
    			else if (SearchTaxonActivity.this.criteria.equalsIgnoreCase("VernacularName")){
    				Log.i(getResources().getString(R.string.app_name), "Search by VernacularName");
    				lstTaxonOccurence = SearchTaxonActivity.this.taxonManager.findByVernacularName(SearchTaxonActivity.this.taxonomy, strSearch, 1000);
    					

    			}    		
    			else{
    				Log.i(getResources().getString(R.string.app_name), "Undefined criteria is: " + SearchTaxonActivity.this.criteria);
    			}
    		}else{
    			Log.i(getResources().getString(R.string.app_name), "Species Manager is NULL!");
    		}   	
    	    	
        	//Close connection
        	JdbcDaoSupport.close();
            handler.sendEmptyMessage(0);
        }

        private Handler handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                populateResultList(lstTaxonOccurence, parentTaxonFieldId);
                pd.dismiss();
            }
        };
        
        private void populateResultList(List<TaxonOccurrence> lstTaxonOccurence, final int parentTaxonFieldId){
        	Log.i("SearchTaxonActivity", "Size of result list is: " + lstTaxonOccurence.size());
        	String[] arrResults = new String[lstTaxonOccurence.size()];
        	int idx = 0;
    		for (TaxonOccurrence taxonOcc : lstTaxonOccurence) {
    			arrResults[idx] = taxonOcc.getCode() + "\n" + taxonOcc.getScientificName() + " ;\n" 
    				+ taxonOcc.getVernacularName()/* + " ;\n" + taxonOcc.getLanguageCode() + " ;\n" 
    				+ taxonOcc.getLanguageVariety()+ " ;\n"*/;
    			arrResults[idx] = arrResults[idx].replaceAll("null", "");
    			idx++;
    		}   
    		SearchTaxonActivity.this.lstResult.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    		SearchTaxonActivity.this.lstResult.setCacheColorHint(Color.TRANSPARENT);
    		SearchTaxonActivity.this.lstResult.requestFocus(0);
    		//Create and set adapter for result list
    		int layout = (backgroundColor!=Color.WHITE)?R.layout.localclusterrow_white:R.layout.localclusterrow_black;	
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchTaxonActivity.this.getApplicationContext(), layout, R.id.plotlabel, arrResults);
            SearchTaxonActivity.this.lstResult.setAdapter(adapter);
            SearchTaxonActivity.this.lblSearch.setText("Search results: ");
    		changeBackgroundColor(SearchTaxonActivity.this.backgroundColor);
        	//Set item click listener 
    		SearchTaxonActivity.this.lstResult.setOnItemClickListener(new OnItemClickListener(){
    			@Override
    			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
    				// Back to previous screen and pass chosen results there
    				String strItem = lstResult.getAdapter().getItem(position).toString();
    				String[] arrItemValues = strItem.replaceAll(";\n", " ;").split(";");
    				for(int i=0; i<arrItemValues.length;i++){
    					Log.i(getResources().getString(R.string.app_name), "i = " + i + "; Value is: " + arrItemValues[i]);
    				}
    				// Set textboxes in TaxonField by given values
    				TaxonField parentTaxonField = (TaxonField)ApplicationManager.getUIElement(parentTaxonFieldId);
    				if(parentTaxonField != null){
    					parentTaxonField.setValue(0, arrItemValues[0], arrItemValues[1], arrItemValues[2], arrItemValues[4], arrItemValues[3], SearchTaxonActivity.this.path,false);
    				}
    				else{
    					Log.i(getResources().getString(R.string.app_name), "Parent taxon field is: NULL");
    				}
    			    // Finish activity
    			    finish();				
    			}
        	});
        }
    }
}
