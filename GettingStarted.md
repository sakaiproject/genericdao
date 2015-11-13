Using Generic DAO (gendao) is meant to be easy and my hopes are that it is.

Some basics are available here (outline of the basic config steps):
## Example Code ##
Projects using generic DAO would create a POJO like this:
```
public class BlogWowBlog {
    private String id;
    private String locationId;
    private String title;
    private String profile;
    private Date dateCreated;

    // Constructors and Getters and Setters
...
}
```
They would create a DAO service like this:
```
public class BlogWowDaoImpl extends HibernateGeneralGenericDao {
   // add additional DAO methods beyond those provided by generic dao here if desired
}
```
And finally use the DAO in their services like this:
```
public class BlogLogicImpl implements BlogLogic {

   private BlogWowDaoImpl dao;
   public void setDao(BlogWowDaoImpl dao) {
      this.dao = dao;
   }

   public BlogWowBlog getBlogByLocationAndUser(String locationId, String userId) {
      List<BlogWowBlog> l = dao.findBySearch(BlogWowBlog.class, new Search("location", locationId) );

      if (l.size() <= 0) {
         // no blog found, create a new one
         if (canWriteBlog(null, locationId, userId)) {
            BlogWowBlog blog = new BlogWowBlog(userId, locationId, "Initial title", new Date());
            dao.save(blog);
            return blog;
         }
         return null;
      } else if (l.size() >= 1) {
         // found existing blog
         return (BlogWowBlog) l.get(0);
      }
   }
...
}
```

The entire source is located in the jar package which you can get from the [Maven2Repo](Maven2Repo.md). The source includes a large number of unit test cases which also serve as example code and demonstrations of how to use the package. This is the place to start if you want to see what is involved in using the package.

Please feel free to email the project owner(s) if you have questions or suggestions.