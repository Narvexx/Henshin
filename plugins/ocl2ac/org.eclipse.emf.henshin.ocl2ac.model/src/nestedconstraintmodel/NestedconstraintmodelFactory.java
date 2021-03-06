/**
 */
package nestedconstraintmodel;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see nestedconstraintmodel.NestedconstraintmodelPackage
 * @generated
 */
public interface NestedconstraintmodelFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	NestedconstraintmodelFactory eINSTANCE = nestedconstraintmodel.impl.NestedconstraintmodelFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Nested Constraint Model</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Nested Constraint Model</em>'.
	 * @generated
	 */
	NestedConstraintModel createNestedConstraintModel();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	NestedconstraintmodelPackage getNestedconstraintmodelPackage();

} //NestedconstraintmodelFactory
