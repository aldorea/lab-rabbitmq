class JobCompletion{
   private String worker;
   private String image;
   private long tsCreationMessage;
   private long tsReceptionWorker;
   private long tsFinalizationWorker;
   // Constructor
   public JobCompletion(){}

    /**
     * @return the worker
     */
    public String getWorker() {
        return worker;
    }

    /**
     * @return the image
     */
    public String getImage() {
        return image;
    }

    /**
     * @return the tsCreationMessage
     */
    public long getTsCreationMessage() {
        return tsCreationMessage;
    }

    /**
     * @param tsCreationMessage the tsCreationMessage to set
     */
    public void setTsCreationMessage(long tsCreationMessage) {
        this.tsCreationMessage = tsCreationMessage;
    }

    /**
     * @return the tsReceptionWorker
     */
    public long getTsReceptionWorker() {
        return tsReceptionWorker;
    }

    /**
     * @param tsReceptionWorker the tsReceptionWorker to set
     */
    public void setTsReceptionWorker(long tsReceptionWorker) {
        this.tsReceptionWorker = tsReceptionWorker;
    }

    /**
     * @return the tsFinalizationWorker
     */
    public long getTsFinalizationWorker() {
        return tsFinalizationWorker;
    }

    /**
     * @param tsFinalizationWorker the tsFinalizationWorker to set
     */
    public void setTsFinalizationWorker(long tsFinalizationWorker) {
        this.tsFinalizationWorker = tsFinalizationWorker;
    }
    
    public void setImage(String image) {
        this.image = image;
    }
    
    public void setWorker(String worker) {
        this.worker = worker;
    }
}
