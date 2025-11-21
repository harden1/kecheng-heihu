// stores/inspection.js
export const useInspectionStore = defineStore('inspection', {
  state: () => ({
    currentData: null
  }),
  actions: {
    setCurrentData(data) {
      this.currentData = data
    },
    clearCurrentData() {
      this.currentData = null
    }
  }
})
