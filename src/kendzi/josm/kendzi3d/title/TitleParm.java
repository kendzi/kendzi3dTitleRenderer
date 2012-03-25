package kendzi.josm.kendzi3d.title;
enum TitleParm {

        DATA_SET_BOUNDS("dataSetBounds"),
        IN_FILE("inFile"),
        OUT_DIR("outDir"),
        RES_DIR("resDir"),
        RENDER_PROPERTIES("renderProperties"),
        X("x"),
        Y("y"),
        ZOOM("zoom");

        String parm;

        TitleParm(String pParm) {
            this.parm = pParm;
        }

        /**
         * @return the parm
         */
        public String getParm() {
            return parm;
        }
    }
