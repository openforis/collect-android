package org.openforis.collect.android.fields;

import org.openforis.idm.metamodel.NodeDefinition;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.widget.EditText;

public class InputField extends Field implements TextWatcher {
	
	public EditText txtBox;
	
	public InputField(Context context, NodeDefinition nodeDef) {
		super(context, nodeDef);
		this.txtBox = new EditText(context);
		this.txtBox.addTextChangedListener(this);
	}
	
	public void setKeyboardType(KeyListener keyListener){
		this.txtBox.setKeyListener(keyListener);
	}
	
	public String getHint()
	{
		return this.txtBox.getHint().toString();
	}
	
	public void setHint(String value)
	{
		this.txtBox.setHint(value);
	}
	
	public void setAlignment(int alignment){
		this.txtBox.setGravity(alignment);
	}
	
	public void makeReal()
	{
		this.txtBox.setKeyListener(new DigitsKeyListener(true,true));		
	}
	
	public void makeInteger()
	{
		this.txtBox.setKeyListener(new DigitsKeyListener(true,false));
	}

	@Override
	public void afterTextChanged(Editable s) {

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}
	
	public void addTextChangedListener(TextWatcher textWatcher) {
		this.txtBox.addTextChangedListener(textWatcher);
	}
}
