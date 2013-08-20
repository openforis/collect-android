package org.openforis.collect.android.database;

import static org.openforis.collect.persistence.jooq.tables.OfcCodeList.OFC_CODE_LIST;

import java.util.ArrayList;
import java.util.List;

import org.jooq.TableField;
import org.openforis.collect.android.management.ApplicationManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.PersistedCodeListItem;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
//import org.jooq.SelectQuery;
//import org.jooq.Record;
//import org.jooq.Result;
//import org.jooq.SelectQuery;
//import org.openforis.collect.persistence.CodeListItemDao.JooqFactory;


public class CodeListItemDao extends org.openforis.collect.persistence.CodeListItemDao {

	public CodeListItemDao() {
		super();
	}
	
	@Override
	protected List<PersistedCodeListItem> loadChildItems(CodeList codeList, Integer parentItemId, ModelVersion version) {
		long startTime = System.currentTimeMillis();
		List<PersistedCodeListItem> result = new ArrayList<PersistedCodeListItem>();
		//Log.e("Mobile DAO", "Starts loading child item: " + System.currentTimeMillis());	
		//Prepare query
		CollectSurvey survey = (CollectSurvey) codeList.getSurvey();
		TableField<OfcCodeListRecord, Integer> surveyIdField = getSurveyIdField(survey.isWork());		
		//Check if parent_id is NULL
		String parentIdCondition = "";
		if (parentItemId == null){
			parentIdCondition = OFC_CODE_LIST.PARENT_ID + " is null";
		}else{
			parentIdCondition = OFC_CODE_LIST.PARENT_ID + " = " + parentItemId;
		}
		//Query string
		String query = "select * from " + OFC_CODE_LIST 
				+ " where " + surveyIdField + " = " + survey.getId()
				+ " and " + OFC_CODE_LIST.CODE_LIST_ID + " = " + codeList.getId()
				+ " and " + parentIdCondition
				+ " order by " + OFC_CODE_LIST.SORT_ORDER; 
		
		//Log.e("Mobile DAO", "Query is: " + query);
		Log.e("Mobile DAO", codeList.getId()+"CodeList is: " + codeList.getName());
		//Execute query
		SQLiteDatabase db = DatabaseHelper.getDb();
		Cursor cursor = db.rawQuery(query, null);
		//Log.e("Mobile DAO", "Number of rows is: " + cursor.getCount());
		//Close database
		db.close();
		//Prepare result
		while (cursor.moveToNext()) {
			/*for (int i=0;i<cursor.getColumnCount();i++){
				Log.e(cursor.getColumnName(i)+"=","=="+cursor.getString(i));
			}*/
			CodeListItem codeListItem = new CodeListItem(codeList, codeList.getId());
			//codeListItem.setAnnotation(qname, value);
			//Log.e("CODE","=="+cursor.getString(cursor.getColumnIndex(OFC_CODE_LIST.CODE.getName())));
			codeListItem.setCode(cursor.getString(cursor.getColumnIndex(OFC_CODE_LIST.CODE.getName())));
			//codeListItem.setDeprecatedVersion(deprecated);
			//codeListItem.setDeprecatedVersionByName(name);
			//codeListItem.setLabel(language, text);
			//codeListItem.setParentItem(parentItem);
			codeListItem.setQualifiable(Boolean.valueOf(cursor.getString(cursor.getColumnIndex(OFC_CODE_LIST.QUALIFIABLE.getName()))));
			//codeListItem.setSinceVersion(since);
			//codeListItem.setSinceVersionByName(name);
			//codeListItem.addDescription(description);
			
			//LanguageSpecificText label = new LanguageSpecificText(ApplicationManager.selectedLanguage,cursor.getString(cursor.getColumnIndex(OFC_CODE_LIST.LABEL1.getName())));
			//codeListItem.addLabel(label);
			List<String> languageList = codeList.getSurvey().getLanguages();
			//Log.e("languagesNo",languageList.get(0)+"=="+languageList.size());
			//Log.e("LABEL","=="+cursor.getString(cursor.getColumnIndex(OFC_CODE_LIST.LABEL1.getName())));
			LanguageSpecificText label = new LanguageSpecificText(languageList.get(0),cursor.getString(cursor.getColumnIndex(OFC_CODE_LIST.LABEL1.getName())));
			codeListItem.addLabel(label);
			if (cursor.getString(cursor.getColumnIndex(OFC_CODE_LIST.LABEL2.getName()))!=null){
				//Log.e("LABEL","=="+cursor.getString(cursor.getColumnIndex(OFC_CODE_LIST.LABEL2.getName())));
				label = new LanguageSpecificText(languageList.get(1),cursor.getString(cursor.getColumnIndex(OFC_CODE_LIST.LABEL2.getName())));
				codeListItem.addLabel(label);
			}
			if (cursor.getString(cursor.getColumnIndex(OFC_CODE_LIST.LABEL3.getName()))!=null){
				//Log.e("LABEL","=="+cursor.getString(cursor.getColumnIndex(OFC_CODE_LIST.LABEL3.getName())));
				label = new LanguageSpecificText(languageList.get(2),cursor.getString(cursor.getColumnIndex(OFC_CODE_LIST.LABEL3.getName())));
				codeListItem.addLabel(label);
			}
			
			PersistedCodeListItem item = PersistedCodeListItem.fromItem(codeListItem);
			result.add(item);
		}
		//Log.e("Mobile DAO", "Ready to return child item: " + System.currentTimeMillis());
		Log.e("MOBILE DAO", "Total time: "+(System.currentTimeMillis()-startTime));
		return result;
	}
	
}
