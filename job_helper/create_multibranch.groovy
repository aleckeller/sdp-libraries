import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import jenkins.plugins.git.GitSCMSource;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.UserRemoteConfig;
import jenkins.branch.BranchSource;
import jenkins.plugins.git.traits.BranchDiscoveryTrait;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.impl.trait.WildcardSCMHeadFilterTrait;
import jenkins.plugins.git.traits.CloneOptionTrait;
import hudson.plugins.git.extensions.impl.CloneOption;
import org.boozallen.plugins.jte.config.TemplateConfigFolderProperty;
import org.boozallen.plugins.jte.config.GovernanceTier;
import org.boozallen.plugins.jte.job.TemplateBranchProjectFactory;
import jenkins.model.Jenkins;
import org.boozallen.plugins.jte.config.TemplateLibrarySource
import org.boozallen.plugins.jte.config.TemplateConfigFolderProperty
import org.boozallen.plugins.jte.config.GovernanceTier
import hudson.plugins.git.*
import com.cloudbees.hudson.plugins.folder.computed.PeriodicFolderTrigger

void call(String path, Map config){
    def jenkins = Jenkins.instance;
    def pipeline_folder = jenkins.getItemByFullName(path.substring(0, path.lastIndexOf("/")))
    def multi_branch_job = new WorkflowMultiBranchProject(pipeline_folder, path.substring(path.lastIndexOf("/") + 1));
    def scm_source = new GitSCMSource(config.project_repository);
    scm_source.setCredentialsId(config.credentials);
    List<SCMSourceTrait> traits = new ArrayList<>();
    traits.add(new BranchDiscoveryTrait());
    traits.add(new WildcardSCMHeadFilterTrait(config.branches,""));
    if (config.clone_option){
        def reference = config.clone_option.reference ?: ""
        def shallow = config.clone_option.shallow ?: false
        def timeout = config.clone_option.timeout ?: null
        node {
            if (!fileExists(reference)){
                def reference_path = reference.substring(0, reference.lastIndexOf("/"))
                def repo_location = ""
                withGit (url: config.project_repository, cred: config.credentials, {
                    repo_location = pwd()
                })
                sh "mkdir --parents ${reference_path}; mv ${repo_location} ${reference_path}"
            }
        }
        CloneOptionTrait clone_trait = new CloneOptionTrait(new CloneOption(shallow, reference, timeout))
        traits.add(clone_trait)
    }
    scm_source.setTraits(traits);
    def branch_source = new BranchSource(scm_source);
    List<BranchSource> sources = new ArrayList<>();
    sources.add(branch_source);
    multi_branch_job.setSourcesList(sources);
    multi_branch_job.setProjectFactory(new TemplateBranchProjectFactory());
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
        multi_branch_job.addProperty(property);
    }
    if (config.periodic_trigger){
        multi_branch_job.addTrigger(periodic_trigger(config.periodic_trigger))
    }
    pipeline_folder.add(multi_branch_job, multi_branch_job.name);
}

def periodic_trigger(String interval){
    return new PeriodicFolderTrigger(interval)
}
