/**
* Copyright � Sergey Melnik (Stanford University, Database Group)
*
* All Rights Reserved.
*/

package org.w3c.rdf.util;

import edu.stanford.db.rdf.model.i.ModelImpl;
import org.w3c.rdf.implementation.syntax.sirpac.SiRS;
import org.w3c.rdf.model.Model;
import org.w3c.rdf.model.NodeFactory;
import org.w3c.rdf.syntax.RDFParser;
import org.w3c.rdf.syntax.RDFSerializer;

//import org.w3c.rdf.implementation.model.*;
//import org.w3c.rdf.implementation.syntax.strawman.*;

/**
* A default implementation of the RDFFactory interface.
*
* @author Sergey Melnik <melnik@db.stanford.edu>
*/

public class RDFFactoryImpl implements RDFFactory {

    // use strawman parser
  //    boolean strawman = false;

  //  static SchemaRegistry registry;

  /*
  public SchemaRegistry registry() {
    if(registry == null)
      registry = new SchemaRegistryImpl( this );
    return registry;
  }
  */

    public RDFFactoryImpl() {
    }

//     public RDFFactoryImpl(boolean strawman) {
// 	this.strawman = strawman;
//     }

  public RDFParser createParser() {

////       if(strawman)
//// 	  return new StrawmanParser();
////       else {
//	  SiRPAC parser = new SiRPAC();
//	  parser.setRobustMode(true);
//	  parser.ignoreExternalEntities(true);
//	  return parser;
//	  //      }
    return null;
  }

  public RDFSerializer createSerializer() {

//     Map ns = new HashMap();
//     ns.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "MYRDF");
//     return new SiRS(ns);
    return new SiRS();
  }

  public Model createModel() {

    return new ModelImpl();
  }

  public NodeFactory getNodeFactory() {

    return new ModelImpl().getNodeFactory();
  }

  /*
  public SchemaModel createSchemaModel(Model m) {

    if(m instanceof SchemaModel)
      return (SchemaModel)m;
    return new SchemaModelImpl( (SchemaRegistryImpl)registry(), m );
  }
  */
}

