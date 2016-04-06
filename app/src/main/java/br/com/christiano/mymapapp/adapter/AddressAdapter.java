package br.com.christiano.mymapapp.adapter;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import br.com.christiano.mymapapp.R;

/**
 * Created by Christiano on 31/03/2016.
 *
 * https://bitbucket.org/chrisvale/mymapapp/
 */
public class AddressAdapter extends ArrayAdapter<Address> {
    private LayoutInflater vi;

    public AddressAdapter(Context context) {
        super(context, 0);
        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = vi.inflate(R.layout.row_address, null);
        }
        ImageView imageView = (ImageView) view.findViewById(R.id.imageIcon);
        imageView.setImageResource(R.drawable.android_place);

        Address item = getItem(position);
        TextView txtAddress = (TextView) view.findViewById(R.id.txtAddress);
        StringBuilder descricao = new StringBuilder();
        if (item.getFeatureName() != null) {
            descricao.append(item.getFeatureName());
        }
        if (item.getAdminArea() != null) {
            descricao.append(", ").append(item.getAdminArea());
        }
        if (item.getCountryCode() != null) {
            descricao.append(", ").append(item.getCountryCode());
        }
        txtAddress.setText(descricao.toString());
        return view;
    }
}
