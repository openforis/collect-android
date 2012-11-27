package org.openforis.collect.android.fields;

import java.util.List;

import org.openforis.collect.android.messages.ToastMessage;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NumberField extends InputField {
	
	private List<String> values;
	private String type;
	
	public NumberField(Context context, String labelText, String initialText, String hintText,
			String numberType, boolean isMultiple) {
		super(context, isMultiple);
		
		this.label = new TextView(context);
		this.label.setMaxLines(1);
		this.label.setText(labelText);
		this.label.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, (float) 2));
		this.label.setOnLongClickListener(new OnLongClickListener() {
	        @Override
	        public boolean onLongClick(View v) {
	        	ToastMessage.displayToastMessage(NumberField.this.getContext(), NumberField.this.getLabelText(), Toast.LENGTH_LONG);
	            return true;
	        }
	    });
		this.txtBox = new EditText(context);
		this.setHint(hintText);
		this.txtBox.setLayoutParams(new LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,(float) 2));
		
		this.type = numberType;
		if (this.type.toLowerCase().equals("integer")){
			this.makeInteger();
		} else{
			this.makeReal();
		}
		
		this.addView(this.scrollLeft);
		this.addView(this.label);
		this.addView(this.txtBox);
		this.addView(this.scrollRight);
	}

	@Override
	public void scrollLeft(){
    	if (NumberField.this.currentInstanceNo>1){
    		NumberField.this.values.set(NumberField.this.currentInstanceNo-1, NumberField.this.txtBox.getText().toString());	        		
    		NumberField.this.txtBox.setText(NumberField.this.values.get(NumberField.this.currentInstanceNo-2));
    		NumberField.this.currentInstanceNo--;
    	}
	}
	
	@Override
	public void scrollRight(){
    	if (NumberField.this.values.size()==NumberField.this.currentInstanceNo){
    		NumberField.this.values.add(NumberField.this.currentInstanceNo, "");	        		
    	}
    	NumberField.this.values.set(NumberField.this.currentInstanceNo-1, NumberField.this.txtBox.getText().toString());        			        		
		if (NumberField.this.values.size()>NumberField.this.currentInstanceNo)
			NumberField.this.txtBox.setText(NumberField.this.values.get(NumberField.this.currentInstanceNo));
		NumberField.this.currentInstanceNo++;
	}	
}
