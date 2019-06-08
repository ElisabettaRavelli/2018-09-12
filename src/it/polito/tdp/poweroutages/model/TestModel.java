package it.polito.tdp.poweroutages.model;

public class TestModel {

	public static void main(String[] args) {
		Model model = new Model();
		model.creaGrafo();
		model.simula(4);
	}

}
