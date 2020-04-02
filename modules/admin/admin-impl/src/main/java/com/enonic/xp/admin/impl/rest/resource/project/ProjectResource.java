package com.enonic.xp.admin.impl.rest.resource.project;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.enonic.xp.admin.impl.rest.resource.ResourceConstants;
import com.enonic.xp.admin.impl.rest.resource.project.json.DeleteProjectParamsJson;
import com.enonic.xp.admin.impl.rest.resource.project.json.ProjectJson;
import com.enonic.xp.admin.impl.rest.resource.project.json.ProjectReadAccessJson;
import com.enonic.xp.admin.impl.rest.resource.project.json.ProjectsJson;
import com.enonic.xp.attachment.CreateAttachment;
import com.enonic.xp.content.ContentService;
import com.enonic.xp.jaxrs.JaxRsComponent;
import com.enonic.xp.project.CreateProjectParams;
import com.enonic.xp.project.ModifyProjectParams;
import com.enonic.xp.project.Project;
import com.enonic.xp.project.ProjectConstants;
import com.enonic.xp.project.ProjectName;
import com.enonic.xp.project.ProjectPermissions;
import com.enonic.xp.project.ProjectService;
import com.enonic.xp.security.PrincipalKeys;
import com.enonic.xp.security.RoleKeys;
import com.enonic.xp.task.TaskService;
import com.enonic.xp.web.HttpStatus;
import com.enonic.xp.web.multipart.MultipartForm;
import com.enonic.xp.web.multipart.MultipartItem;

import static com.enonic.xp.project.ProjectConstants.PROJECT_ACCESS_LEVEL_AUTHOR_PROPERTY;
import static com.enonic.xp.project.ProjectConstants.PROJECT_ACCESS_LEVEL_CONTRIBUTOR_PROPERTY;
import static com.enonic.xp.project.ProjectConstants.PROJECT_ACCESS_LEVEL_EDITOR_PROPERTY;
import static com.enonic.xp.project.ProjectConstants.PROJECT_ACCESS_LEVEL_OWNER_PROPERTY;

@SuppressWarnings("UnusedDeclaration")
@Path(ResourceConstants.REST_ROOT + "project")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({RoleKeys.ADMIN_ID, RoleKeys.ADMIN_LOGIN_ID})
@Component(immediate = true, property = "group=admin")
public final class ProjectResource
    implements JaxRsComponent
{
    private static final Logger LOG = LoggerFactory.getLogger( ProjectResource.class );

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ProjectService projectService;

    private TaskService taskService;

    private ContentService contentService;

    @POST
    @Path("create")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public ProjectJson create( final MultipartForm form )
        throws Exception
    {
        final Project project = projectService.create( createParams( form ) );
        applyProjectData( project, form );

        return doCreateJson( project );
    }

    @POST
    @Path("modify")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public ProjectJson modify( final MultipartForm form )
        throws Exception
    {
        final Project modifiedProject = this.projectService.modify( ModifyProjectParams.create( createParams( form ) ).build() );

        if ( ProjectConstants.DEFAULT_PROJECT_NAME.equals( modifiedProject.getName() ) )
        {
            final Locale language = doApplyLanguage( modifiedProject.getName(), getLanguageFromForm( form ) );
            return doCreateJson( modifiedProject, null, null, language );
        }

        applyProjectData( modifiedProject, form );

        return doCreateJson( modifiedProject );
    }

    @POST
    @Path("delete")
    public boolean delete( final DeleteProjectParamsJson params )
    {
        if ( ProjectConstants.DEFAULT_PROJECT_NAME.equals( params.getName() ) )
        {
            throw new WebApplicationException( "Default repo is not allowed to be deleted", HttpStatus.METHOD_NOT_ALLOWED.value() );
        }

        return this.projectService.delete( params.getName() );
    }

    @GET
    @Path("list")
    public ProjectsJson list()
    {
        final List<ProjectJson> projects = this.projectService.list().stream().
            map( this::doCreateJson ).
            collect( Collectors.toList() );

        return new ProjectsJson( projects );
    }

    @GET
    @Path("get")
    public ProjectJson get( final @QueryParam("name") String projectNameValue )
    {
        final ProjectName projectName = ProjectName.from( projectNameValue );
        return doCreateJson( this.projectService.get( projectName ) );
    }

    private CreateProjectParams createParams( final MultipartForm form )
        throws IOException
    {
        final CreateProjectParams.Builder builder = CreateProjectParams.create().
            name( ProjectName.from( form.getAsString( "name" ) ) ).
            displayName( form.getAsString( "displayName" ) ).
            description( form.getAsString( "description" ) );

        final MultipartItem icon = form.get( "icon" );

        if ( icon != null )
        {
            builder.icon( CreateAttachment.create().
                name( ProjectConstants.PROJECT_ICON_PROPERTY ).
                mimeType( icon.getContentType().toString() ).
                byteSource( icon.getBytes() ).
                build() );
        }

        return builder.build();
    }

    private Locale getLanguageFromForm( final MultipartForm form )
    {
        return Optional.ofNullable( form.getAsString( "language" ) ).
            map( Locale::forLanguageTag ).
            orElse( null );
    }

    private ProjectReadAccess getReadAccessFromForm( final MultipartForm form )
    {
        final ProjectReadAccess.Builder readAccess = ProjectReadAccess.create();

        return Optional.ofNullable( form.getAsString( "readAccess" ) ).
            map( readAccessAsString -> {
                try
                {
                    return MAPPER.readValue( readAccessAsString, ProjectReadAccessJson.class );
                }
                catch ( IOException e )
                {
                    throw new UncheckedIOException( e );
                }
            } ).
            map( ProjectReadAccessJson::getProjectReadAccess ).
            orElseGet( readAccess::build );
    }

    private ProjectPermissions.Builder getPermissionsFromForm( final MultipartForm form )
        throws IOException
    {
        final ProjectPermissions.Builder builder = new ProjectPermissions.Builder();
        final String permissionsAsString = form.getAsString( "permissions" );
        if ( permissionsAsString == null )
        {
            return builder;
        }

        final Map<String, List<String>> map = MAPPER.readValue( permissionsAsString, new TypeReference<Map<String, List<String>>>()
        {
        } );

        if ( map.containsKey( PROJECT_ACCESS_LEVEL_OWNER_PROPERTY ) )
        {
            map.get( PROJECT_ACCESS_LEVEL_OWNER_PROPERTY ).forEach( builder::addOwner );
        }

        if ( map.containsKey( PROJECT_ACCESS_LEVEL_EDITOR_PROPERTY ) )
        {
            map.get( PROJECT_ACCESS_LEVEL_EDITOR_PROPERTY ).forEach( builder::addEditor );
        }

        if ( map.containsKey( PROJECT_ACCESS_LEVEL_AUTHOR_PROPERTY ) )
        {
            map.get( PROJECT_ACCESS_LEVEL_AUTHOR_PROPERTY ).forEach( builder::addAuthor );
        }

        if ( map.containsKey( PROJECT_ACCESS_LEVEL_CONTRIBUTOR_PROPERTY ) )
        {
            map.get( PROJECT_ACCESS_LEVEL_CONTRIBUTOR_PROPERTY ).forEach( builder::addContributor );
        }

        return builder;
    }

    private ProjectJson doCreateJson( final Project project, final ProjectPermissions projectPermissions,
                                      final ProjectReadAccessType readAccessType, final Locale language )
    {
        return new ProjectJson( project, projectPermissions, readAccessType, language );
    }

    private ProjectJson doCreateJson( final Project project )
    {
        final ProjectName projectName = project.getName();

        ProjectPermissions projectPermissions = null;
        ProjectReadAccessType readAccessType = null;

        if ( !ProjectConstants.DEFAULT_PROJECT_NAME.equals( projectName ) )
        {
            projectPermissions = doFetchPermissions( projectName );
            readAccessType = doFetchReadAccess( projectName, projectPermissions.getViewer() ).getType();
        }

        final Locale language = doFetchLanguage( projectName );

        return doCreateJson( project, projectPermissions, readAccessType, language );
    }

    private void applyProjectData( final Project project, final MultipartForm form )
        throws IOException
    {
        final ProjectName projectName = project.getName();

        final ProjectPermissions.Builder projectPermissionsBuilder = getPermissionsFromForm( form );
        final ProjectReadAccess readAccess = getReadAccessFromForm( form );
        final Locale language = getLanguageFromForm( form );

        final ProjectPermissions projectPermissions = doAddViewerRoleMembers( projectPermissionsBuilder, readAccess ).build();

        doApplyPermissions( projectName, projectPermissions );
        doApplyReadAccess( projectName, readAccess );
        doApplyLanguage( projectName, language );
    }

    private ProjectPermissions doFetchPermissions( final ProjectName projectName )
    {
        return this.projectService.getPermissions( projectName );
    }

    private ProjectReadAccess doFetchReadAccess( final ProjectName projectName, final PrincipalKeys viewerRoleMembers )
    {
        return GetProjectReadAccessCommand.create().
            viewerRoleMembers( viewerRoleMembers ).
            projectName( projectName ).
            contentService( contentService ).
            build().
            execute();
    }

    private Locale doFetchLanguage( final ProjectName projectName )
    {
        return GetProjectLanguageCommand.create().
            projectName( projectName ).
            contentService( contentService ).
            build().
            execute();
    }

    private ProjectPermissions doApplyPermissions( final ProjectName projectName, final ProjectPermissions projectPermissions )
    {
        return projectService.modifyPermissions( projectName, projectPermissions );
    }

    private Locale doApplyLanguage( final ProjectName projectName, final Locale language )
    {
        return ApplyProjectLanguageCommand.create().
            projectName( projectName ).
            language( language ).
            contentService( contentService ).
            build().
            execute();
    }

    private void doApplyReadAccess( final ProjectName projectName, final ProjectReadAccess readAccess )
    {
        ApplyProjectReadAccessPermissionsCommand.create().
            projectName( projectName ).
            readAccess( readAccess ).
            taskService( taskService ).
            contentService( contentService ).
            build().
            execute();
    }

    private ProjectPermissions.Builder doAddViewerRoleMembers( final ProjectPermissions.Builder builder,
                                                               final ProjectReadAccess readAccess )
    {
        if ( ProjectReadAccessType.CUSTOM.equals( readAccess.getType() ) )
        {
            readAccess.getPrincipals().forEach( builder::addViewer );
        }

        return builder;
    }

    @Reference
    public void setProjectService( final ProjectService projectService )
    {
        this.projectService = projectService;
    }

    @Reference
    public void setContentService( final ContentService contentService )
    {
        this.contentService = contentService;
    }

    @Reference
    public void setTaskService( final TaskService taskService )
    {
        this.taskService = taskService;
    }
}
