package pl.edu.pw.ee.cosplay.client.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;

import pl.edu.pw.ee.cosplay.R;
import pl.edu.pw.ee.cosplay.client.activity.LoginActivity;
import pl.edu.pw.ee.cosplay.client.activity.MenuActivity;
import pl.edu.pw.ee.cosplay.client.activity.PhotoActivity;
import pl.edu.pw.ee.cosplay.client.networking.ServerTask;
import pl.edu.pw.ee.cosplay.client.utils.Utils;
import pl.edu.pw.ee.cosplay.rest.model.constants.UrlData;
import pl.edu.pw.ee.cosplay.rest.model.controller.photos.addphoto.AddPhotoInput;
import pl.edu.pw.ee.cosplay.rest.model.controller.photos.addphoto.AddPhotoOutput;
import pl.edu.pw.ee.cosplay.rest.model.controller.photos.getphoto.GetPhotoInput;
import pl.edu.pw.ee.cosplay.rest.model.controller.photos.getphoto.GetPhotoOutput;
import pl.edu.pw.ee.cosplay.rest.model.security.AuthenticationData;

public class AddPhotoFragment extends Fragment implements View.OnClickListener{

    private ImageView selectedPhotoImageView;
    private AuthenticationData authenticationData;
    private EditText charactersEditText;
    private EditText descriptionEditText;
    private EditText franchisesEditText;
    private View v;

    public AddPhotoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        v =  inflater.inflate(R.layout.fragment_add_photo, container, false);

        selectedPhotoImageView = (ImageView) v.findViewById(R.id.selectedPhotoImageView);
        charactersEditText = (EditText) v.findViewById(R.id.charactersEditText);
        descriptionEditText = (EditText) v.findViewById(R.id.descriptionEditText);
        franchisesEditText = (EditText) v.findViewById(R.id.franchisesEditText);

        authenticationData = LoginActivity.authenticationData;

        Button addPhotoButton = (Button) v.findViewById(R.id.addPhotoButton);
        addPhotoButton.setOnClickListener(this);

        Button selectPhotoButton = (Button) v.findViewById(R.id.selectPhotoButton);
        selectPhotoButton.setOnClickListener(this);

        return v;
    }

    public void selectPhotoButtonAction() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        Intent chooserIntent = Intent.createChooser(getIntent, "Select method");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent, cameraIntent});

        startActivityForResult(chooserIntent, Integer.valueOf(0));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }
            try {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                selectedPhotoImageView.setImageBitmap(photo);
                v.findViewById(R.id.addPhotoButton).setVisibility(View.VISIBLE);
                v.findViewById(R.id.selectedPhotoImageView).setVisibility(View.VISIBLE);
            } catch (NullPointerException e){
                try {
                    InputStream photoInputStream = getActivity().getContentResolver().openInputStream(data.getData());
                    selectedPhotoImageView.setImageBitmap(BitmapFactory.decodeStream(photoInputStream));
                    v.findViewById(R.id.addPhotoButton).setVisibility(View.VISIBLE);
                    v.findViewById(R.id.selectedPhotoImageView).setVisibility(View.VISIBLE);
                } catch (FileNotFoundException ignored) {
                }
            }
        }
    }

    public void addPhotoButtonAction() {
        (new ServerTask<AddPhotoInput, AddPhotoOutput, Activity>(getActivity(), null, UrlData.ADD_PHOTO_PATH) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Object doInBackground(Void[] params) {
                this.input = getAddPhotoInput();
                return super.doInBackground(params);
            }

            @Override protected void doSomethingWithOutput(AddPhotoOutput o) {
                final GetPhotoInput getPhotoInput = new GetPhotoInput();
                getPhotoInput.setPhotoId(o.getAddedPhotoId());
                Intent intent = new Intent(activity, PhotoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(MenuActivity.GET_PHOTO_ID, getPhotoInput);
                intent.putExtras(bundle);
                activity.startActivity(intent);
            }
        }).execute();
    }

    private AddPhotoInput getAddPhotoInput(){
        AddPhotoInput input = new AddPhotoInput();

        input.setAuthenticationData(authenticationData);
        input.setPhotoDescription(descriptionEditText.getText().toString());

        HashSet<String> franchisesList = Utils.parseToList(franchisesEditText.getText().toString());
        HashSet<String> charactersList = Utils.parseToList(charactersEditText.getText().toString());

        input.setCharactersList(charactersList);
        input.setFranchisesList(franchisesList);

        byte[] bytes = Utils.getBytesFromImageView(selectedPhotoImageView);
        input.setPhotoBinaryData(bytes);

        return input;
    }


    @Override
    public void onClick(View vi) {
        switch (vi.getId()){
            case R.id.addPhotoButton:{
                addPhotoButtonAction();
                break;
            }
            case R.id.selectPhotoButton:{
                selectPhotoButtonAction();
                break;
            }
        }
    }
}
