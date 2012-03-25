package kendzi.josm.kendzi3d.title;

import java.util.ArrayList;
import java.util.List;

import kendzi.jogl.model.render.ModelRender;
import kendzi.josm.kendzi3d.jogl.RenderJOSM;
import kendzi.josm.kendzi3d.jogl.layer.BuildingLayer;
import kendzi.josm.kendzi3d.jogl.layer.FenceLayer;
import kendzi.josm.kendzi3d.jogl.layer.Layer;
import kendzi.josm.kendzi3d.jogl.layer.PointModelsLayer;
import kendzi.josm.kendzi3d.jogl.layer.RoadLayer;
import kendzi.josm.kendzi3d.jogl.layer.TreeLayer;
import kendzi.josm.kendzi3d.jogl.layer.WaterLayer;
import kendzi.josm.kendzi3d.jogl.photos.PhotoRenderer;
import kendzi.josm.kendzi3d.module.binding.Kendzi3dPluginDirectory;
import kendzi.josm.kendzi3d.service.ColorTextureBuilder;
import kendzi.josm.kendzi3d.service.MetadataCacheService;
import kendzi.josm.kendzi3d.service.ModelCacheService;
import kendzi.josm.kendzi3d.service.TextureCacheService;
import kendzi.josm.kendzi3d.service.UrlReciverService;
import kendzi.josm.kendzi3d.service.WikiTextureLoaderService;
import kendzi.josm.kendzi3d.service.impl.FileUrlReciverService;
import kendzi.josm.kendzi3d.service.impl.PointModelService;
import kendzi.josm.kendzi3d.ui.Kendzi3dGLEventListener;
import kendzi.josm.kendzi3d.ui.Kendzi3dGLFrame;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class TitleModule extends AbstractModule {
    private final String pluginDirectory;

    /**
     * @param pPluginDirectory
     *            the URL of the foo server.
     */
    public TitleModule(String pPluginDirectory) {
        this.pluginDirectory = pPluginDirectory;
    }

    @Override
    protected void configure() {

        bindConstant().annotatedWith(Kendzi3dPluginDirectory.class).to(this.pluginDirectory);

        /*
         * This tells Guice that whenever it sees a dependency on a TransactionLog, it should satisfy the dependency
         * using a DatabaseTransactionLog.
         */
        bind(UrlReciverService.class).to(FileUrlReciverService.class);

//        bind(ColorTextureBuilder.class);

        bind(MetadataCacheService.class).in(Singleton.class);

        bind(WikiTextureLoaderService.class).in(Singleton.class);

        bind(PointModelService.class).in(Singleton.class);

        bind(ModelRender.class).in(Singleton.class);

//        bind(PointModelsLayer.class);

        bind(PointModelsLayer.class);
        bind(BuildingLayer.class);
        bind(RoadLayer.class);
        bind(WaterLayer.class);
        bind(TreeLayer.class);
        bind(FenceLayer.class);

        bind(PhotoRenderer.class);

        bind(Kendzi3dGLEventListener.class).in(Singleton.class);

        bind(Kendzi3dGLFrame.class);

        // /*
        // * Similarly, this binding tells Guice that when CreditCardProcessor is used in
        // * a dependency, that should be satisfied with a PaypalCreditCardProcessor.
        // */
        // bind(CreditCardProcessor.class).to(PaypalCreditCardProcessor.class);

    }

    @Provides @Singleton
    TextureCacheService provideTextureCacheService(UrlReciverService pUrlReciverService) {
        TextureCacheService textureCacheService = new TextureCacheService();
        textureCacheService.setFileUrlReciverService(pUrlReciverService);
        textureCacheService.addTextureBuilder(new ColorTextureBuilder());
        return textureCacheService;
    }

//    @Provides
//    PointModelsLayer providePointModelsLayer(
//            UrlReciverService pUrlReciverService,
//            ModelRender pModelRender,
//            ModelCacheService modelCacheService) {
//
//        PointModelsLayer pointModelsLayer = new PointModelsLayer();
//        pointModelsLayer.setUrlReciverService(pUrlReciverService);
//        pointModelsLayer.setModelRender(pModelRender);
//        pointModelsLayer.setModelCacheService(modelCacheService);
//        pointModelsLayer.init();
//
//        return pointModelsLayer;
//    }

    @Provides @Singleton
    RenderJOSM provideRenderJOSM(
            ModelRender pModelRender,
            PointModelsLayer pointModelsLayer,
            BuildingLayer buildingLayer,
            RoadLayer roadLayer,
            WaterLayer waterLayer,
            TreeLayer treeLayer,
            FenceLayer fenceLayer

    ) {

        // PointModelsLayer pointModelsLayer = new PointModelsLayer();
        // pointModelsLayer.setModelRender(modelRender);
        // pointModelsLayer.setFileUrlReciverService(fileUrlReciverService);
        // <bean id="exampleInitBean" class="examples.ExampleBean" init-method="init"/>
        // pointModelsLayer.init();

        // BuildingLayer buildingLayer = new BuildingLayer();
        // buildingLayer.setModelRender(modelRender);

        // RoadLayer roadLayer = new RoadLayer();
        // roadLayer.setModelRender(modelRender);
        //
        // WaterLayer waterLayer = new WaterLayer();
        // waterLayer.setModelRender(modelRender);

        // TreeLayer treeLayer = new TreeLayer();
        // treeLayer.setModelRender(modelRender);

        // FenceLayer fenceLayer = new FenceLayer();
        // fenceLayer.setModelRender(modelRender);

        List<Layer> layerList = new ArrayList<Layer>();
        layerList.add(pointModelsLayer);
        layerList.add(buildingLayer);
        layerList.add(roadLayer);
        layerList.add(waterLayer);
        layerList.add(treeLayer);
        layerList.add(fenceLayer);

        RenderJOSM renderJOSM = new RenderJOSM();
        renderJOSM.setModelRender(pModelRender);
        renderJOSM.setLayerList(layerList);

        return renderJOSM;
    }
}
