package br.org.reembolsofacil.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;
import br.org.reembolsofacil.R;
import br.org.reembolsofacil.pojo.EntViagem;

public class ViagensArrayAdapter extends ArrayAdapter<EntViagem> {
	Activity context;
	List<EntViagem> items;
	private LayoutInflater mInflater;

	//SearchHeader header;

	public ViagensArrayAdapter(final Activity context,
	        /*final Map<SearchHeader,*/ List<EntViagem> result) 
	{
        super(context, android.R.layout.simple_spinner_dropdown_item, result);
        this.context = context;
        //this.header = result.keySet().iterator().next();
        //this.items = result.get(this.header);
        this.items = result;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	@Override
	public View getView(final int position, final View convertView,
	        final ViewGroup parent) {
		
	        final View view = this.context.getLayoutInflater().inflate(
	        		android.R.layout.simple_spinner_item, null);
	        if(items.size()>0){
		        final EntViagem item = this.items.get(position);
		        ((TextView) view.findViewById(android.R.id.text1)).setText(item.getMotivoViagem());
	        }
	        /*((TextView) view.findViewById(R.id.dp)).setText(item.dp);
	        ((TextView) view.findViewById(R.id.cn)).setText(item.cn);
	        ((TextView) view.findViewById(R.id.loc)).setText(item.loc.name);*/
	        
	        /*final TextView body = ((TextView) view.findViewById(R.id.e));
	        body.setText(item.e);
	        body.setTag(item.src[0]);*/
	        
	        //((TextView) view.findViewById(R.id.src)).setText(item.src[1]);
	        
	        return view;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		
		return createViewFromResource(position, convertView, parent, R.layout.row_viagem);

		//return super.getDropDownView(position, convertView, parent);
	}
	
    private View createViewFromResource(int position, View convertView, ViewGroup parent,
            int resource) {
        View view;
        CheckedTextView cText;

        if (convertView == null) {
            view = mInflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }

        /*try {
            if (mFieldId == 0) {
                //  If no custom field is assigned, assume the whole resource is a TextView
                text = (TextView) view;
            } else {
                //  Otherwise, find the TextView field within the layout
                text = (TextView) view.findViewById(mFieldId);
            }
        } catch (ClassCastException e) {
            Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    "ArrayAdapter requires the resource ID to be a TextView", e);
        }*/
        
        EntViagem item = getItem(position);
        cText = (CheckedTextView) view.findViewById(android.R.id.text1);
        cText.setText("de: " + item.getDataInicViagem() + "\naté: " + item.getDataFimViagem()
        		+ "\nmotivo: "+item.getMotivoViagem());
        /*if (item instanceof CharSequence) {
            text.setText((CharSequence)item);
        } else {
            text.setText(item.toString());
        }*/

        return view;
    }

	
}
