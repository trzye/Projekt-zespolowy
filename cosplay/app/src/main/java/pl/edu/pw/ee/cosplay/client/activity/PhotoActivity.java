package pl.edu.pw.ee.cosplay.client.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;

import pl.edu.pw.ee.cosplay.R;
import pl.edu.pw.ee.cosplay.client.adapter.OnePhotoAdapter;
import pl.edu.pw.ee.cosplay.client.fragment.UserFragment;
import pl.edu.pw.ee.cosplay.client.networking.ServerTask;
import pl.edu.pw.ee.cosplay.client.utils.Utils;
import pl.edu.pw.ee.cosplay.rest.model.constants.UrlData;
import pl.edu.pw.ee.cosplay.rest.model.controller.photos.addcomment.AddCommentInput;
import pl.edu.pw.ee.cosplay.rest.model.controller.photos.addcomment.AddCommentOutput;
import pl.edu.pw.ee.cosplay.rest.model.controller.photos.getphoto.GetPhotoInput;
import pl.edu.pw.ee.cosplay.rest.model.controller.photos.getphoto.GetPhotoOutput;
import pl.edu.pw.ee.cosplay.rest.model.controller.photos.getphotoslist.GetPhotosListInput;
import pl.edu.pw.ee.cosplay.rest.model.controller.photos.getphotoslist.GetPhotosListOutput;
import pl.edu.pw.ee.cosplay.rest.model.controller.photos.getphotoslist.PhotosOrder;
import pl.edu.pw.ee.cosplay.rest.model.controller.user.GetUserInput;
import pl.edu.pw.ee.cosplay.rest.model.controller.user.GetUserOutput;
import pl.edu.pw.ee.cosplay.rest.model.security.AuthenticationData;

public class PhotoActivity extends AppCompatActivity {

    GetPhotoInput getPhotoInput;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        getPhotoInput = (GetPhotoInput) getIntent().getSerializableExtra(MenuActivity.GET_PHOTO_ID);
        final RelativeLayout onePhotoLayoutId = (RelativeLayout) findViewById(R.id.onePhotoLayoutId);
        onePhotoLayoutId.setVisibility(View.INVISIBLE);

        (new ServerTask<GetPhotoInput, GetPhotoOutput, PhotoActivity>(this, getPhotoInput, UrlData.GET_PHOTO_PATH){

            @Override
            protected void doSomethingWithOutput(GetPhotoOutput o) {
                setActivityData(o);
                OnePhotoAdapter onePhotoAdapter = new OnePhotoAdapter(activity, R.layout.comment_item, o.getComments());
                ListView listView = (ListView) findViewById(R.id.commentListView);
                listView.setAdapter(onePhotoAdapter);
                onePhotoLayoutId.setVisibility(View.VISIBLE);
            }
        }).execute();
        context = this;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void setActivityData(final GetPhotoOutput data) {
        TextView oneArrangementTextView = (TextView) findViewById(R.id.oneArrangementTextView);
        TextView oneCharacterTextView = (TextView) findViewById(R.id.oneCharacterTextView);
        TextView oneDateTextView = (TextView) findViewById(R.id.oneDateTextView);
        TextView oneDescriptionTextView = (TextView) findViewById(R.id.oneDescriptionTextView);
        TextView oneFranchiseTextView = (TextView) findViewById(R.id.oneFranchiseTextView);
        TextView oneGeneralTextView = (TextView) findViewById(R.id.oneGeneralTextView);
        TextView oneQualityTextView = (TextView) findViewById(R.id.oneQualityTextView);
        TextView oneSimilarityTextView = (TextView) findViewById(R.id.oneSimilarityTextView);
        TextView oneUserTextView = (TextView) findViewById(R.id.oneUserTextView);
        ImageView oneAvatarImageButton = (ImageView) findViewById(R.id.oneAvatarImageButton);
        ImageView onePhotoImageView = (ImageView) findViewById(R.id.onePhotoImageView);

        //Photo
        Utils.setImageViewByBytesArray(onePhotoImageView, data.getPhotoBinaryData());

        //Rating
        oneGeneralTextView.setText(data.getRatingData().getGeneralRate().toString());
        oneQualityTextView.setText(data.getRatingData().getQualityRate().toString());
        oneSimilarityTextView.setText(data.getRatingData().getSimilarityRate().toString());
        oneArrangementTextView.setText(data.getRatingData().getArrangementRate().toString());

        //Lists
        oneCharacterTextView.setText(Utils.parseReadableList(data.getCharactersList()));
        oneFranchiseTextView.setText(Utils.parseReadableList(data.getFranchisesList()));

        //Des
        oneUserTextView.setText(data.getUsername());
        setTitle(data.getUsername() + "'s photo");
        oneDateTextView.setText(Utils.formatDate(data.getUploadDate()));
        oneDescriptionTextView.setText(data.getDescription());

        //Avatar
        if(data.getAvatarBinaryData() != null){
            Utils.setImageViewByBytesArray(oneAvatarImageButton, data.getAvatarBinaryData());
        }

        oneAvatarImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, MenuActivity.class);
                i.putExtra(MenuActivity.USERNAME_ID, data.getUsername());
                startActivity(i);
                MenuActivity.fa.finish();
                finish();
            }
        });
    }

    public void addCommentClick(View view) {
        EditText editText = (EditText) findViewById(R.id.commentEditText);
        AddCommentInput addCommentInput = new AddCommentInput();
        addCommentInput.setAuthenticationData(LoginActivity.authenticationData);
        addCommentInput.setComment(editText.getText().toString());
        addCommentInput.setPhotoId(getPhotoInput.getPhotoId());
        (new ServerTask<AddCommentInput, AddCommentOutput, PhotoActivity>(this, addCommentInput, UrlData.ADD_COMMENT_CONTROLLER) {
                    @Override
                    protected void doSomethingWithOutput(AddCommentOutput o) {
                        Toast.makeText(activity, "Comment sent", Toast.LENGTH_SHORT).show();
                    }
        }).execute();
    }


}
