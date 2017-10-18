  <th width="0"><input style="" type="checkbox" onClick="toggle(this)" name="" value="" ><label style="margin-left: 5px;">Tất cả</label> </th>
 <td><input type="checkbox" name="id[]" class="checkbox" value="{!!$value['id']!!}"></td>  
<script type="text/javascript">
// Xóa người dùng
 // Chọn TẤT CẢ
  $.ajaxSetup({
            headers: {
                'X-CSRF-TOKEN': $('meta[name="_token"]').attr('content')
            }
        });

 function toggle(source) {
  checkboxes = document.getElementsByName('id[]');
  for(var i=0, n=checkboxes.length;i<n;i++) {
    checkboxes[i].checked = source.checked;
  }
}
// Truyền dữ liệu xóa
 


 //Xóa từ khóa
    $("#Delete").click(function(){

      var r = confirm("Bạn có muốn xóa người dùng đã chọn?");
        if (r == true) {
         var id = [];  
       $(':checkbox:checked').each(function(i){  
         id[i] = $(this).val();  
        });  

         $.ajax({                   
          type : 'get',          
          url : 'user_delete_many', //Here you will fetch records 
          data : {id:id},         
          success : function(data){
          
            $('#result').html(data);//Show fetched data from database
          }
        });
            setTimeout(function() 
  {
    location.reload(); 
      alert("Xóa thành công");
          
  }, 200);
        
       } else {
        txt = "You pressed Cancel!";
       }
      
      });
    

</script>
<script type="text/javascript">

     $("#Save").click(function(){    
        
         $.ajaxSetup({
            headers: {
                'X-CSRF-TOKEN': $('meta[name="_token"]').attr('content')
            }
        });
            var id = $('#_id').val(); 
            var userName = $('#edtName').val();
            var email = $('#edtEmail').val();
            var txtPass = $('#edtPass').val();
            var level  = $("input[type='radio'].rdLevel:checked").val();    
            var status  = $("input[type='radio'].rdStatus:checked").val();    
            if ($("#cbEmail").is(":checked"))
{
  var receive_email  = 1;
 
  
} else receive_email  = null;


     
          var data = {
               id :id,
              txtUser : userName,
              txtEmail     : email,  
              txtPass      : txtPass,
              rdoLevel : level,
              rdoStatus : status,
              cbEmail:receive_email,
              _token : $('meta[name="_token"]').attr('content'),            
        };
          $.ajax({                   
          type : 'get',          
          url : 'edit-user', //Here you will fetch records 
          data : data,         
          success : function(data){
            $('#result').html(data);//Show fetched data from database
          }
        });
            
         
         
     });
</script>