import jenkins.branch.OrganizationFolder
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator
import jenkins.scm.api.trait.SCMSourceTrait
import jenkins.scm.impl.trait.WildcardSCMSourceFilterTrait
import org.jenkinsci.plugins.github_branch_source.*
import org.boozallen.plugins.jte.job.*
import org.boozallen.plugins.jte.config.*
import hudson.plugins.git.UserRemoteConfig;
import hudson.plugins.git.*
import jenkins.model.Jenkins;


void call(String path, Map config){
    def jenkins = Jenkins.instance
    def pipeline_folder = jenkins.getItemByFullName(path.substring(0, path.lastIndexOf("/")))
    def github_org_job = new OrganizationFolder(pipeline_folder, path.substring(path.lastIndexOf("/") + 1));

    //SCM Navigator
    GitHubSCMNavigator scm_nav = new GitHubSCMNavigator(config.organization)
    scm_nav.setCredentialsId(config.credentials_id)
    scm_nav.setApiUri(config.api_uri)

    //Behaviors
    List<SCMSourceTrait> traits = new ArrayList<>();
    traits.add(new WildcardSCMSourceFilterTrait(config.includes,""))
    traits.add(new BranchDiscoveryTrait(1))
    traits.add(new OriginPullRequestDiscoveryTrait(1))
    scm_nav.setTraits(traits)

    //Project Factory
    github_org_job.getProjectFactories().replace(new TemplateMultiBranchProjectFactory())
    github_org_job.getNavigators().replace(scm_nav)

    //JTE Configuration
    if (config.jte){
        def scm = null
        def base_directory = null
        List<TemplateLibrarySource> library_sources = new ArrayList<>()
        if (config.jte.config){
            List<UserRemoteConfig> pipeline_config_list = new ArrayList<>();
            pipeline_config_list.add(new UserRemoteConfig(config.jte.config.url,null,null,config.jte.config.credentials));
            scm = new GitSCM(pipeline_config_list,null,null,null,null,null,null);
            base_directory = config.jte.config.base_directory
        }
        if (config.jte.library_sources){
            TemplateLibrarySource library_source = new TemplateLibrarySource()
            def repository_url = (config.jte.library_sources.scm.repository_url) ? config.jte.library_sources.scm.repository_url : ""
            def branch_specifier = (config.jte.library_sources.scm.branch_specifier) ? config.jte.library_sources.scm.branch_specifier : ""
            def credentials_id = (config.jte.library_sources.scm.credentials_id) ? config.jte.library_sources.scm.credentials_id : ""
            def library_base_directory = (config.jte.library_sources.base_directory) ? config.jte.library_sources.base_directory : ""
            
            List<BranchSpec> branch_list = new ArrayList<>();
            branch_list.add(new BranchSpec(branch_specifier));
            List<UserRemoteConfig> library_list = new ArrayList<>();
            library_list.add(new UserRemoteConfig(repository_url,null,null,credentials_id));
            def source_scm = new GitSCM(library_list,branch_list,null,null,null,null,null)
            library_source.setScm(source_scm)
            library_source.setBaseDir(library_base_directory)
            library_sources.add(library_source)
        }
        GovernanceTier tier = new GovernanceTier(scm,base_directory,library_sources);
        TemplateConfigFolderProperty property = new TemplateConfigFolderProperty(tier);
        github_org_job.addProperty(property);
    }
    pipeline_folder.add(github_org_job,github_org_job.name)
}
