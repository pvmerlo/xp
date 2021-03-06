package com.enonic.xp.admin.impl.rest.resource.schema.mixin;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;

import com.google.common.io.ByteStreams;

import com.enonic.xp.admin.impl.rest.resource.AdminResourceTestSupport;
import com.enonic.xp.form.Form;
import com.enonic.xp.form.Input;
import com.enonic.xp.i18n.LocaleService;
import com.enonic.xp.i18n.MessageBundle;
import com.enonic.xp.icon.Icon;
import com.enonic.xp.inputtype.InputTypeName;
import com.enonic.xp.jaxrs.impl.MockRestResponse;
import com.enonic.xp.schema.mixin.Mixin;
import com.enonic.xp.schema.mixin.MixinName;
import com.enonic.xp.schema.mixin.MixinService;
import com.enonic.xp.schema.mixin.Mixins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

public class MixinResourceTest
    extends AdminResourceTestSupport
{
    private static final MixinName MY_MIXIN_QUALIFIED_NAME_1 = MixinName.from( "myapplication:input_text_1" );

    private static final String MY_MIXIN_INPUT_NAME_1 = "input_text_1";

    private static final MixinName MY_MIXIN_QUALIFIED_NAME_2 = MixinName.from( "myapplication:text_area_2" );

    private static final String MY_MIXIN_INPUT_NAME_2 = "text_area_2";

    private MixinService mixinService;

    private LocaleService localeService;

    private MixinResource resource;

    @Override
    protected Object getResourceInstance()
    {
        mixinService = Mockito.mock( MixinService.class );
        localeService = Mockito.mock( LocaleService.class );

        resource = new MixinResource();
        resource.setMixinService( mixinService );
        resource.setLocaleService( localeService );

        return resource;
    }

    @Test
    public final void test_get_mixin()
        throws Exception
    {
        Mixin mixin = Mixin.create().
            createdTime( LocalDateTime.of( 2013, 1, 1, 12, 0, 0 ).toInstant( ZoneOffset.UTC ) ).
            name( MY_MIXIN_QUALIFIED_NAME_1.toString() ).addFormItem(
            Input.create().name( MY_MIXIN_INPUT_NAME_1 ).inputType( InputTypeName.TEXT_LINE ).label( "Line Text 1" ).required(
                true ).helpText( "Help text line 1" ).required( true ).build() ).build();

        Mockito.when( mixinService.getByName( Mockito.isA( MixinName.class ) ) ).thenReturn( mixin );
        Mockito.when( mixinService.inlineFormItems( Mockito.isA( Form.class ) ) ).then( AdditionalAnswers.returnsFirstArg() );

        String response = request().path( "schema/mixin" ).queryParam( "name", MY_MIXIN_QUALIFIED_NAME_1.toString() ).get().getAsString();

        assertJson( "get_mixin.json", response );
    }

    @Test
    public final void test_get_mixin_i18n()
        throws Exception
    {
        Mixin mixin = Mixin.create().
            createdTime( LocalDateTime.of( 2013, 1, 1, 12, 0, 0 ).toInstant( ZoneOffset.UTC ) ).
            displayNameI18nKey( "key.display-name" ).
            descriptionI18nKey( "key.description" ).
            name( MY_MIXIN_QUALIFIED_NAME_1.toString() ).addFormItem(
            Input.create().name( MY_MIXIN_INPUT_NAME_1 ).inputType( InputTypeName.TEXT_LINE ).label( "Line Text 1" ).required(
                true ).labelI18nKey( "key.label" ).helpText( "Help text line 1" ).helpTextI18nKey( "key.help-text" ).required(
                true ).build() ).build();

        Mockito.when( mixinService.getByName( Mockito.isA( MixinName.class ) ) ).thenReturn( mixin );

        final MessageBundle messageBundle = Mockito.mock( MessageBundle.class );
        Mockito.when( messageBundle.localize( "key.label" ) ).thenReturn( "translated.label" );
        Mockito.when( messageBundle.localize( "key.help-text" ) ).thenReturn( "translated.helpText" );
        Mockito.when( messageBundle.localize( "key.display-name" ) ).thenReturn( "translated.displayName" );
        Mockito.when( messageBundle.localize( "key.description" ) ).thenReturn( "translated.description" );

        Mockito.when( this.localeService.getBundle( any(), any() ) ).thenReturn( messageBundle );
        Mockito.when( mixinService.inlineFormItems( Mockito.isA( Form.class ) ) ).then( AdditionalAnswers.returnsFirstArg() );

        String response = request().path( "schema/mixin" ).queryParam( "name", MY_MIXIN_QUALIFIED_NAME_1.toString() ).get().getAsString();

        assertJson( "get_mixin_i18n.json", response );
    }

    @Test
    public final void test_get_mixin_not_found()
        throws Exception
    {
        Mockito.when( mixinService.getByName( any( MixinName.class ) ) ).thenReturn( null );

        final MockRestResponse response = request().path( "schema/mixin" ).queryParam( "name", MY_MIXIN_QUALIFIED_NAME_1.toString() ).get();
        assertEquals( 404, response.getStatus() );
    }

    @Test
    public final void test_list_mixins()
        throws Exception
    {
        Mixin mixin1 = Mixin.create().createdTime( LocalDateTime.of( 2013, 1, 1, 12, 0, 0 ).toInstant( ZoneOffset.UTC ) ).name(
            MY_MIXIN_QUALIFIED_NAME_1.toString() ).addFormItem(
            Input.create().name( MY_MIXIN_INPUT_NAME_1 ).inputType( InputTypeName.TEXT_LINE ).label( "Line Text 1" ).required(
                true ).helpText( "Help text line 1" ).required( true ).build() ).build();

        Mixin mixin2 = Mixin.create().createdTime( LocalDateTime.of( 2013, 1, 1, 12, 0, 0 ).toInstant( ZoneOffset.UTC ) ).name(
            MY_MIXIN_QUALIFIED_NAME_2.toString() ).addFormItem(
            Input.create().name( MY_MIXIN_INPUT_NAME_2 ).inputType( InputTypeName.TEXT_AREA ).label( "Text Area" ).required(
                true ).helpText( "Help text area" ).required( true ).build() ).build();

        Mockito.when( mixinService.getAll() ).thenReturn( Mixins.from( mixin1, mixin2 ) );
        Mockito.when( mixinService.inlineFormItems( Mockito.isA( Form.class ) ) ).then( AdditionalAnswers.returnsFirstArg() );


        String result = request().path( "schema/mixin/list" ).get().getAsString();

        assertJson( "list_mixins.json", result );
    }

    @Test
    public void testMixinIcon()
        throws Exception
    {
        final byte[] data;
        try (InputStream stream = getClass().getResourceAsStream( "mixinicon.png" ))
        {
            data = stream.readAllBytes();
        }
        final Icon icon = Icon.from( data, "image/png", Instant.now() );

        Mixin mixin = Mixin.create().
            name( "myapplication:postal_code" ).
            displayName( "My content type" ).
            icon( icon ).
            addFormItem( Input.create().name( "postal_code" ).label( "Postal code" ).inputType( InputTypeName.TEXT_LINE ).build() ).
            build();
        setupMixin( mixin );

        // exercise
        final Response response = this.resource.getIcon( "myapplication:postal_code", 20, null );
        final BufferedImage mixinIcon = (BufferedImage) response.getEntity();

        // verify
        assertImage( mixinIcon, 20 );
    }

    @Test
    public void testMixinIcon_default_image()
        throws Exception
    {
        final InputStream in = getClass().getResourceAsStream( "mixin.svg" );
        final Response response = this.resource.getIcon( "myapplication:icon_svg_test", 20, null );

        assertNotNull( response.getEntity() );
        Assertions.assertArrayEquals( ByteStreams.toByteArray( in ), (byte[]) response.getEntity() );
    }

    @Test
    public void getIconIsSvg()
        throws Exception
    {
        final byte[] data;
        try (InputStream stream = getClass().getResourceAsStream( "icon-black.svg" ))
        {
            data = stream.readAllBytes();
        }
        final Icon icon = Icon.from( data, "image/svg+xml", Instant.now() );

        Mixin mixin = Mixin.create().
            name( "myapplication:icon_svg_test" ).
            displayName( "My content type" ).
            icon( icon ).
            addFormItem( Input.create().name( "icon_svg_test" ).label( "SVG icon test" ).inputType( InputTypeName.TEXT_LINE ).build() ).
            build();
        setupMixin( mixin );

        final Response response = this.resource.getIcon( "myapplication:icon_svg_test", 20, null );

        assertNotNull( response.getEntity() );
        assertEquals( icon.getMimeType(), response.getMediaType().toString() );
        Assertions.assertArrayEquals( data, (byte[]) response.getEntity() );
    }

    private void setupMixin( final Mixin mixin )
    {
        Mockito.when( mixinService.getByName( mixin.getName() ) ).thenReturn( mixin );
    }

    private void assertImage( final BufferedImage image, final int size )
    {
        assertNotNull( image );
        assertEquals( size, image.getWidth() );
    }

}
